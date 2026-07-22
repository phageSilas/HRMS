package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.config.ApprovalNodeDef;
import com.hrms.business.approval.dto.ApprovalDetailVO;
import com.hrms.business.approval.dto.PendingTaskQuery;
import com.hrms.business.approval.dto.PendingTaskVO;
import com.hrms.business.approval.entity.ApprovalInstanceEntity;
import com.hrms.business.approval.entity.ApprovalTaskEntity;
import com.hrms.business.approval.enums.ApprovalStatusEnum;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.enums.ApproveResultEnum;
import com.hrms.business.approval.enums.TaskStatusEnum;
import com.hrms.business.approval.mapper.ApprovalInstanceMapper;
import com.hrms.business.approval.mapper.ApprovalTaskMapper;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.approval.service.ApprovalTemplateLoader;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 审批任务服务实现
 * <p>
 * 提供待办任务查询、已办任务查询、我发起的申请、审批详情查询、撤回审批等核心功能。
 * 待办任务通过分页查询 {@link ApprovalTaskEntity} 并关联 {@link ApprovalInstanceEntity} 获取完整的审批上下文。
 * 查询中显式添加 is_deleted = 0 条件作为 @TableLogic 逻辑删除的双重保险。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTaskServiceImpl implements ApprovalTaskService {

    /** 日期时间格式化器：yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalTemplateLoader templateLoader;
    private final ApprovalEngine approvalEngine;
    private final UserMapper userMapper;

    /**
     * 查询待办任务列表（分页）
     * <p>
     * 先根据筛选条件查询符合条件的实例 ID，再基于这些实例分页查询当前用户的待办任务。
     * 额外返回全局待办总数用于前端角标展示。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param query  分页及筛选条件
     * @return 分页待办任务结果（含 badgeCount 角标数）
     */
    @Override
    public PageResult<PendingTaskVO> findPendingTasks(Long userId, PendingTaskQuery query) {
        // 1. 按筛选条件查询符合条件的实例ID
        List<Long> instanceIds = queryFilteredInstanceIds(query);
        if (instanceIds.isEmpty()) {
            return emptyPageResult(query);
        }

        // 2. 分页查询当前用户的待办任务（通过 EXISTS 子查询确保实例未被逻辑删除）
        Page<ApprovalTaskEntity> mpPage = new Page<>(query.getPageNum(), query.getPageSize());
        mpPage = taskMapper.selectPage(mpPage, Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                .in(ApprovalTaskEntity::getInstanceId, instanceIds)
                .eq(ApprovalTaskEntity::getApproverUserId, userId)
                .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                .apply("EXISTS (SELECT 1 FROM hr_approval_instance i WHERE i.id = instance_id AND i.is_deleted = 0)")
                .orderByDesc(ApprovalTaskEntity::getCreateTime)
        );

        // 3. 查询该用户全局待办总数（用于角标）
        Integer badgeCount = Math.toIntExact(taskMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getApproverUserId, userId)
                        .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
        ));

        PageResult<PendingTaskVO> result = buildTaskPageResult(mpPage);
        result.setBadgeCount(badgeCount);
        return result;
    }

    /**
     * 查询已办任务列表（分页）
     * <p>
     * 与待办查询逻辑相似，但只查询已处理（PROCESSED）和已转交（TRANSFERRED）的任务，
     * 并按审批时间倒序排列。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param query  分页及筛选条件
     * @return 分页已办任务结果
     */
    @Override
    public PageResult<PendingTaskVO> findHistoryTasks(Long userId, PendingTaskQuery query) {
        List<Long> instanceIds = queryFilteredInstanceIds(query);
        if (instanceIds.isEmpty()) {
            return emptyPageResult(query);
        }

        Page<ApprovalTaskEntity> mpPage = new Page<>(query.getPageNum(), query.getPageSize());
        mpPage = taskMapper.selectPage(mpPage, Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                .in(ApprovalTaskEntity::getInstanceId, instanceIds)
                .eq(ApprovalTaskEntity::getApproverUserId, userId)
                .in(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PROCESSED.getCode(), TaskStatusEnum.TRANSFERRED.getCode())
                .apply("EXISTS (SELECT 1 FROM hr_approval_instance i WHERE i.id = instance_id AND i.is_deleted = 0)")
                .orderByDesc(ApprovalTaskEntity::getApproveTime)
        );

        return buildTaskPageResult(mpPage);
    }

    /**
     * 查询我发起的申请列表（分页）
     * <p>
     * 直接查询审批实例表，按申请人 ID 过滤，关联查询每个实例下所有任务用于构建审批步骤状态。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param query  分页及筛选条件
     * @return 分页申请记录结果
     */
    @Override
    public PageResult<PendingTaskVO> findMyApplications(Long userId, PendingTaskQuery query) {
        // 直接查询实例表
        LambdaQueryWrapper<ApprovalInstanceEntity> wrapper = Wrappers.lambdaQuery(ApprovalInstanceEntity.class)
                .eq(ApprovalInstanceEntity::getApplicantUserId, userId)
                .orderByDesc(ApprovalInstanceEntity::getApplyTime);

        if (org.springframework.util.StringUtils.hasText(query.getBusinessType())) {
            wrapper.eq(ApprovalInstanceEntity::getApprovalType, query.getBusinessType());
        }

        Page<ApprovalInstanceEntity> mpPage = new Page<>(query.getPageNum(), query.getPageSize());
        mpPage = instanceMapper.selectPage(mpPage, wrapper);

        // 查询每个实例关联的任务（用于 Steps 状态）
        Set<Long> instanceIdSet = mpPage.getRecords().stream()
                .map(ApprovalInstanceEntity::getId).collect(Collectors.toSet());
        List<ApprovalTaskEntity> allTasks = instanceIdSet.isEmpty() ? Collections.emptyList() :
                taskMapper.selectList(Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .in(ApprovalTaskEntity::getInstanceId, instanceIdSet));
        Map<Long, List<ApprovalTaskEntity>> taskMap = allTasks.stream()
                .collect(Collectors.groupingBy(ApprovalTaskEntity::getInstanceId));

        List<PendingTaskVO> voList = mpPage.getRecords().stream()
                .map(inst -> buildInstanceVO(inst, taskMap.getOrDefault(inst.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        PageResult<PendingTaskVO> result = new PageResult<>();
        result.setTotal(mpPage.getTotal());
        result.setPageNum((int) mpPage.getCurrent());
        result.setPageSize((int) mpPage.getSize());
        result.setPages((int) mpPage.getPages());
        result.setRecords(voList);
        return result;
    }

    /**
     * 查询审批详情
     * <p>
     * 构建审批详情 VO，包含：审批节点（Steps）、审批历史（Timeline）、
     * 当前操作人标识、表单快照、超期状态等信息。
     * </p>
     *
     * @param instanceId    审批实例 ID
     * @param currentUserId 当前用户 ID（用于判断是否为当前审批人）
     * @return 审批详情 VO
     * @throws GlobalException 审批实例不存在时抛出
     */
    @Override
    public ApprovalDetailVO getDetail(Long instanceId, Long currentUserId) {
        log.debug("查询审批详情: instanceId={}, currentUserId={}", instanceId, currentUserId);
        ApprovalInstanceEntity instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            log.warn("审批实例不存在: instanceId={}, userId={}", instanceId, currentUserId);
            throw new GlobalException(ErrorCode.NOT_FOUND, "审批实例不存在");
        }

        // 查询所有相关任务
        List<ApprovalTaskEntity> tasks = taskMapper.selectList(Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                .eq(ApprovalTaskEntity::getInstanceId, instanceId)
                .orderByAsc(ApprovalTaskEntity::getSortNo)
        );

        // 构建审批节点（Steps）
        List<ApprovalNodeDef> allNodes = templateLoader.loadTemplate(instance.getApprovalType());
        List<ApprovalDetailVO.ApprovalNodeVO> approvalNodes = buildApprovalNodes(allNodes, tasks);

        // 构建审批历史（Timeline）
        List<ApprovalDetailVO.ApprovalHistoryVO> approvalHistory = buildApprovalHistory(tasks);

        // 判断当前用户是否为当前审批人
        boolean currentOperator = tasks.stream()
                .anyMatch(t -> t.getTaskStatus() == TaskStatusEnum.PENDING.getCode()
                        && Objects.equals(t.getApproverUserId(), currentUserId));

        // 获取当前待办任务ID（前端操作用）
        Long currentTaskId = tasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatusEnum.PENDING.getCode()
                        && Objects.equals(t.getApproverUserId(), currentUserId))
                .findFirst()
                .map(ApprovalTaskEntity::getId)
                .orElse(null);

        // 解析表单快照
        Object formData = null;
        if (instance.getFormJson() != null) {
            try {
                formData = new com.fasterxml.jackson.databind.ObjectMapper().readTree(instance.getFormJson());
            } catch (Exception e) {
                formData = instance.getFormJson();
            }
        }

        // 检查是否有待办任务已过期
        boolean hasExpired = tasks.stream()
                .anyMatch(t -> t.getDeadlineTime() != null
                        && t.getDeadlineTime().isBefore(LocalDateTime.now())
                        && Objects.equals(t.getTaskStatus(), TaskStatusEnum.PENDING.getCode()));

        // 构建 VO
        ApprovalDetailVO vo = new ApprovalDetailVO();
        vo.setTitle(instance.getTitle());
        vo.setBusinessType(instance.getApprovalType());
        vo.setBusinessTypeName(getApprovalTypeName(instance.getApprovalType()));
        if (hasExpired) {
            vo.setStatus("EXPIRED");
            vo.setStatusName("已过期");
        } else {
            vo.setStatus(getStatusStr(instance.getApprovalStatus()));
            vo.setStatusName(getStatusName(instance.getApprovalStatus()));
        }
        vo.setApplicantName(getUserName(instance.getApplicantUserId()));
        vo.setCreatedAt(formatTime(instance.getApplyTime()));
        vo.setFormData(formData);
        vo.setApprovalNodes(approvalNodes);
        vo.setApprovalHistory(approvalHistory);
        vo.setCurrentOperator(currentOperator);
        vo.setCurrentTaskId(currentTaskId);

        return vo;
    }

    /**
     * 撤回审批
     * <p>
     * 仅申请人可撤回，仅审批中（PENDING）状态的实例可撤回。
     * 撤回后实例状态变更为 WITHDRAWN，同时取消当前待办任务。
     * </p>
     *
     * @param instanceId 审批实例 ID
     * @param userId     当前用户 ID
     * @throws GlobalException 实例不存在、非本人申请或状态不允许撤回时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId, Long userId) {
        ApprovalInstanceEntity instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "审批实例不存在");
        }
        if (!Objects.equals(instance.getApplicantUserId(), userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "只有申请人才能撤回");
        }
        if (instance.getApprovalStatus() != ApprovalStatusEnum.PENDING.getCode()) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "仅审批中可撤回");
        }

        // 撤回实例
        instance.setApprovalStatus(ApprovalStatusEnum.WITHDRAWN.getCode());
        instance.setFinishTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        // 将当前待处理的任务取消
        taskMapper.update(null, Wrappers.lambdaUpdate(ApprovalTaskEntity.class)
                .set(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PROCESSED.getCode())
                .eq(ApprovalTaskEntity::getInstanceId, instanceId)
                .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
        );

        log.info("审批撤回: instanceId={}, userId={}", instanceId, userId);
    }

    /**
     * 查询用户待办任务总数（用于角标）
     *
     * @param userId 用户 ID
     * @return 待办任务总数
     */
    @Override
    public Integer getPendingCount(Long userId) {
        return Math.toIntExact(taskMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getApproverUserId, userId)
                        .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
        ));
    }

    /**
     * 按筛选类型查询任务列表（分页）
     * <p>
     * 支持三类筛选：pending（待办）、today-approved（今日已审批）、overdue（已逾期）。
     * 待办视图额外返回角标数，其他视图不覆盖角标。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param query  分页及筛选条件（含 filterType）
     * @return 分页任务结果
     */
    @Override
    public PageResult<PendingTaskVO> findFilteredTasks(Long userId, PendingTaskQuery query) {
        // 1. 按筛选条件查询符合条件的实例ID（复用实例级筛选逻辑）
        List<Long> instanceIds = queryFilteredInstanceIds(query);
        if (instanceIds.isEmpty()) {
            return emptyPageResult(query);
        }

        // 2. 根据 filterType 分支构造不同的任务表查询条件
        String filterType = query.getFilterType();
        LocalDateTime now = LocalDateTime.now();

        Page<ApprovalTaskEntity> mpPage = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalTaskEntity> wrapper = Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                .in(ApprovalTaskEntity::getInstanceId, instanceIds)
                .eq(ApprovalTaskEntity::getApproverUserId, userId)
                .apply("EXISTS (SELECT 1 FROM hr_approval_instance i WHERE i.id = instance_id AND i.is_deleted = 0)");

        if ("today-approved".equals(filterType)) {
            // 今日已审批：已处理任务 + 审批时间在今天
            wrapper.in(ApprovalTaskEntity::getTaskStatus,
                            TaskStatusEnum.PROCESSED.getCode(),
                            TaskStatusEnum.TRANSFERRED.getCode())
                    .ge(ApprovalTaskEntity::getApproveTime,
                            now.withHour(0).withMinute(0).withSecond(0).withNano(0))
                    .orderByDesc(ApprovalTaskEntity::getApproveTime);
        } else if ("overdue".equals(filterType)) {
            // 已逾期：待办任务 + 截止时间已过
            wrapper.eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                    .lt(ApprovalTaskEntity::getDeadlineTime, now)
                    .orderByAsc(ApprovalTaskEntity::getCreateTime);
        } else {
            // pending（默认）：待办任务
            wrapper.eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                    .orderByDesc(ApprovalTaskEntity::getCreateTime);
        }

        mpPage = taskMapper.selectPage(mpPage, wrapper);

        // 3. 待办角标仅在 pending 视图有意义，其他视图不覆盖角标
        Integer badgeCount = "today-approved".equals(filterType)
                ? null
                : Math.toIntExact(taskMapper.selectCount(
                        Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                                .eq(ApprovalTaskEntity::getApproverUserId, userId)
                                .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())));

        PageResult<PendingTaskVO> result = buildTaskPageResult(mpPage);
        result.setBadgeCount(badgeCount);
        return result;
    }

    /**
     * 查询用户今日已审批的任务数
     *
     * @param userId 用户 ID
     * @return 今日已审批数
     */
    @Override
    public Integer getTodayApprovedCount(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Math.toIntExact(taskMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getApproverUserId, userId)
                        .in(ApprovalTaskEntity::getTaskStatus,
                                TaskStatusEnum.PROCESSED.getCode(),
                                TaskStatusEnum.TRANSFERRED.getCode())
                        .ge(ApprovalTaskEntity::getApproveTime, todayStart)
        ));
    }

    /**
     * 查询用户逾期的待办任务数
     * <p>
     * 逾期定义为：任务处于待办状态且截止时间已过当前时间。
     * </p>
     *
     * @param userId 用户 ID
     * @return 逾期任务数
     */
    @Override
    public Integer getOverdueCount(Long userId) {
        return Math.toIntExact(taskMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getApproverUserId, userId)
                        .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                        .lt(ApprovalTaskEntity::getDeadlineTime, LocalDateTime.now())
        ));
    }

    /**
     * 按审批实例ID查询当前待办任务ID。
     *
     * @param instanceId 审批实例ID
     * @return 当前待办任务ID，不存在时返回 null
     */
    @Override
    public Long getCurrentPendingTaskIdByInstanceId(Long instanceId) {
        if (instanceId == null) {
            return null;
        }
        ApprovalTaskEntity pendingTask = taskMapper.selectOne(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getInstanceId, instanceId)
                        .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                        .orderByAsc(ApprovalTaskEntity::getSortNo)
                        .last("LIMIT 1")
        );
        return pendingTask == null ? null : pendingTask.getId();
    }

    // ========== 内部方法 ==========

    /**
     * 根据筛选条件查询匹配的实例 ID 列表
     * <p>
     * 显式添加 is_deleted = 0 条件作为 @TableLogic 的双重保险，
     * 确保不会返回已被逻辑删除的实例。
     * </p>
     */
    private List<Long> queryFilteredInstanceIds(PendingTaskQuery query) {
        LambdaQueryWrapper<ApprovalInstanceEntity> wrapper = Wrappers.lambdaQuery();
        // 显式声明 is_deleted = 0（双重保险，@TableLogic 在某些 MyBatis-Plus 版本中
        // 对无实体类型的 Wrappers.lambdaQuery() 可能不生效）
        wrapper.eq(ApprovalInstanceEntity::getIsDeleted, 0);
        if (org.springframework.util.StringUtils.hasText(query.getBusinessType())) {
            wrapper.eq(ApprovalInstanceEntity::getApprovalType, query.getBusinessType());
        }
        if (org.springframework.util.StringUtils.hasText(query.getKeyword())) {
            // 查询匹配的申请人用户ID（支持按申请人姓名搜索）
            List<UserEntity> matchedUsers = userMapper.selectList(
                    Wrappers.lambdaQuery(UserEntity.class)
                            .like(UserEntity::getRealName, query.getKeyword())
                            .eq(UserEntity::getIsDeleted, 0)
            );
            Set<Long> matchedUserIds = matchedUsers.stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toSet());

            // 标题搜索 OR 申请人姓名搜索
            wrapper.and(w -> {
                w.like(ApprovalInstanceEntity::getTitle, query.getKeyword());
                if (!matchedUserIds.isEmpty()) {
                    w.or().in(ApprovalInstanceEntity::getApplicantUserId, matchedUserIds);
                }
            });
        }
        if (org.springframework.util.StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(ApprovalInstanceEntity::getApplyTime,
                    LocalDateTime.parse(query.getStartDate(), DTF));
        }
        if (org.springframework.util.StringUtils.hasText(query.getEndDate())) {
            wrapper.le(ApprovalInstanceEntity::getApplyTime,
                    LocalDateTime.parse(query.getEndDate(), DTF));
        }
        return instanceMapper.selectList(wrapper).stream()
                .map(ApprovalInstanceEntity::getId)
                .collect(Collectors.toList());
    }

    /**
     * 构建任务列表页的分页结果
     */
    private PageResult<PendingTaskVO> buildTaskPageResult(IPage<ApprovalTaskEntity> mpPage) {
        // 批量查询实例
        Set<Long> instanceIds = mpPage.getRecords().stream()
                .map(ApprovalTaskEntity::getInstanceId).collect(Collectors.toSet());
        Map<Long, ApprovalInstanceEntity> instanceMap = instanceIds.isEmpty() ? Collections.emptyMap() :
                instanceMapper.selectBatchIds(instanceIds).stream()
                        .collect(Collectors.toMap(ApprovalInstanceEntity::getId, Function.identity()));

        List<PendingTaskVO> voList = mpPage.getRecords().stream()
                .map(task -> {
                    ApprovalInstanceEntity inst = instanceMap.get(task.getInstanceId());
                    if (inst == null) {
                        log.warn("待办任务引用的审批实例不存在或已被删除: taskId={}, instanceId={}", task.getId(), task.getInstanceId());
                        return null;
                    }
                    return buildTaskVO(task, inst);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果有任务被过滤掉（实例已被删除），记录总数差异
        if (voList.size() < mpPage.getRecords().size()) {
            log.warn("待办列表部分任务因实例不存在被过滤: 原始数={}, 过滤后={}", mpPage.getRecords().size(), voList.size());
        }

        PageResult<PendingTaskVO> result = new PageResult<>();
        result.setTotal(mpPage.getTotal());
        result.setPageNum((int) mpPage.getCurrent());
        result.setPageSize((int) mpPage.getSize());
        result.setPages((int) mpPage.getPages());
        result.setRecords(voList);
        return result;
    }

    /**
     * 构建任务条目 VO
     */
    private PendingTaskVO buildTaskVO(ApprovalTaskEntity task, ApprovalInstanceEntity instance) {
        PendingTaskVO vo = new PendingTaskVO();
        vo.setId(instance.getId());
        vo.setTaskId(task.getId());
        vo.setBusinessType(instance.getApprovalType());
        vo.setBusinessTypeName(getApprovalTypeName(instance.getApprovalType()));
        vo.setTitle(instance.getTitle());
        vo.setApplicantName(getUserName(instance.getApplicantUserId()));
        vo.setNodeName(task.getNodeName());
        vo.setDelegateFlag(task.getDelegateFlag() == 1);
        if (task.getDelegateFlag() == 1) {
            vo.setDelegateMark(getUserName(task.getApproverUserId()) + " 代 "
                    + getUserName(task.getOriginalApproverId()) + " 审批");
        } else {
            vo.setDelegateMark("");
        }
        vo.setCreatedAt(formatTime(instance.getApplyTime()));
        vo.setDeadline(formatTime(task.getDeadlineTime()));
        // 待办的截止时间已过 → 显示"已过期"
        boolean isOverdue = task.getDeadlineTime() != null
                && task.getDeadlineTime().isBefore(LocalDateTime.now())
                && Objects.equals(task.getTaskStatus(), TaskStatusEnum.PENDING.getCode());
        if (isOverdue) {
            vo.setStatus("EXPIRED");
            vo.setStatusName("已过期");
        } else {
            vo.setStatus(getStatusStr(instance.getApprovalStatus()));
            vo.setStatusName(getStatusName(instance.getApprovalStatus()));
        }
        vo.setOverdue(isOverdue);
        return vo;
    }

    /**
     * 构建实例条目 VO（用于"我发起的申请"）
     */
    private PendingTaskVO buildInstanceVO(ApprovalInstanceEntity instance, List<ApprovalTaskEntity> tasks) {
        PendingTaskVO vo = new PendingTaskVO();
        vo.setId(instance.getId());
        // 找当前待办任务的 taskId（可操作撤回）
        tasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatusEnum.PENDING.getCode())
                .findFirst().ifPresent(t -> vo.setTaskId(t.getId()));
        vo.setBusinessType(instance.getApprovalType());
        vo.setBusinessTypeName(getApprovalTypeName(instance.getApprovalType()));
        vo.setTitle(instance.getTitle());
        vo.setApplicantName(getUserName(instance.getApplicantUserId()));
        vo.setNodeName(instance.getCurrentNodeName());
        vo.setDelegateFlag(false);
        vo.setDelegateMark("");
        vo.setCreatedAt(formatTime(instance.getApplyTime()));
        vo.setDeadline(null);
        vo.setStatus(getStatusStr(instance.getApprovalStatus()));
        return vo;
    }

    /**
     * 构建 Steps 审批节点列表
     */
    private List<ApprovalDetailVO.ApprovalNodeVO> buildApprovalNodes(
            List<ApprovalNodeDef> allNodes, List<ApprovalTaskEntity> tasks) {
        // 按 sortNo 建立任务映射
        Map<Integer, ApprovalTaskEntity> taskMap = tasks.stream()
                .collect(Collectors.toMap(ApprovalTaskEntity::getSortNo, Function.identity(), (a, b) -> b));

        List<ApprovalDetailVO.ApprovalNodeVO> result = new ArrayList<>();
        for (ApprovalNodeDef node : allNodes) {
            ApprovalTaskEntity task = taskMap.get(node.getSortNo());
            ApprovalDetailVO.ApprovalNodeVO vo = new ApprovalDetailVO.ApprovalNodeVO();
            vo.setNodeName(node.getNodeName());

            if (task != null) {
                // 有任务记录
                if (task.getTaskStatus() == TaskStatusEnum.PROCESSED.getCode()) {
                    vo.setStatus("completed");
                    vo.setOperatorName(getUserName(task.getApproverUserId()));
                } else if (task.getTaskStatus() == TaskStatusEnum.PENDING.getCode()) {
                    vo.setStatus("current");
                } else {
                    vo.setStatus("completed"); // transferred is also completed
                    vo.setOperatorName(getUserName(task.getApproverUserId()));
                }
                result.add(vo);
            } else if (!node.isOptional()) {
                // 无任务记录且不可选 → 待处理
                vo.setStatus("pending");
                result.add(vo);
            }
            // 可选节点且无任务 → 跳过（不展示）
        }
        return result;
    }

    /**
     * 构建 Timeline 审批历史
     */
    private List<ApprovalDetailVO.ApprovalHistoryVO> buildApprovalHistory(List<ApprovalTaskEntity> tasks) {
        return tasks.stream()
                .filter(t -> t.getTaskStatus() != TaskStatusEnum.PENDING.getCode()) // 只展示已处理的
                .sorted(Comparator.comparing(ApprovalTaskEntity::getSortNo))
                .map(task -> {
                    ApprovalDetailVO.ApprovalHistoryVO vo = new ApprovalDetailVO.ApprovalHistoryVO();
                    // 委托场景下显示 "被委托人 代 委托人 审批"
                    if (task.getDelegateFlag() == 1) {
                        vo.setOperatorName(getUserName(task.getApproverUserId()) + " 代 "
                                + getUserName(task.getOriginalApproverId()));
                    } else {
                        vo.setOperatorName(getUserName(task.getApproverUserId()));
                    }
                    vo.setNodeName(task.getNodeName());
                    vo.setAction(getActionStr(task));
                    vo.setActionName(getActionName(task));
                    vo.setComment(task.getApproveComment());
                    vo.setOperatedAt(formatTime(task.getApproveTime()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ========== 工具方法 ==========

    /**
     * 根据用户ID查询真实姓名
     */
    private String getUserName(Long userId) {
        if (userId == null) return "";
        UserEntity user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : String.valueOf(userId);
    }

    private String getApprovalTypeName(String type) {
        ApprovalTypeEnum e = ApprovalTypeEnum.fromCode(type);
        return e != null ? e.getDesc() : type;
    }

    private String getStatusStr(Integer statusCode) {
        ApprovalStatusEnum e = ApprovalStatusEnum.fromCode(statusCode);
        return e != null ? e.name() : "UNKNOWN";
    }

    private String getStatusName(Integer statusCode) {
        ApprovalStatusEnum e = ApprovalStatusEnum.fromCode(statusCode);
        return e != null ? e.getDesc() : "未知";
    }

    private String getActionStr(ApprovalTaskEntity task) {
        if (task.getApproveResult() == null) return "";
        return switch (task.getApproveResult()) {
            case 1 -> "approve";
            case 2 -> "reject";
            case 3 -> "transfer";
            default -> "";
        };
    }

    private String getActionName(ApprovalTaskEntity task) {
        if (task.getApproveResult() == null) return "";
        ApproveResultEnum e = ApproveResultEnum.fromCode(task.getApproveResult());
        return e != null ? e.getDesc() : "";
    }

    private String formatTime(LocalDateTime time) {
        return time != null ? time.format(DTF) : null;
    }

    private PageResult<PendingTaskVO> emptyPageResult(PendingTaskQuery query) {
        PageResult<PendingTaskVO> result = new PageResult<>();
        result.setTotal(0L);
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        result.setPages(0);
        result.setRecords(Collections.emptyList());
        return result;
    }
}

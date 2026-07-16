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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTaskServiceImpl implements ApprovalTaskService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalTemplateLoader templateLoader;
    private final ApprovalEngine approvalEngine;
    private final UserMapper userMapper;

    @Override
    public PageResult<PendingTaskVO> findPendingTasks(Long userId, PendingTaskQuery query) {
        // 1. 按筛选条件查询符合条件的实例ID
        List<Long> instanceIds = queryFilteredInstanceIds(query);
        if (instanceIds.isEmpty()) {
            return emptyPageResult(query);
        }

        // 2. 分页查询当前用户的待办任务
        Page<ApprovalTaskEntity> mpPage = new Page<>(query.getPageNum(), query.getPageSize());
        mpPage = taskMapper.selectPage(mpPage, Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                .in(ApprovalTaskEntity::getInstanceId, instanceIds)
                .eq(ApprovalTaskEntity::getApproverUserId, userId)
                .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
                .orderByAsc(ApprovalTaskEntity::getCreateTime)
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
                .orderByDesc(ApprovalTaskEntity::getApproveTime)
        );

        return buildTaskPageResult(mpPage);
    }

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

    @Override
    public ApprovalDetailVO getDetail(Long instanceId, Long currentUserId) {
        ApprovalInstanceEntity instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
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

        // 构建 VO
        ApprovalDetailVO vo = new ApprovalDetailVO();
        vo.setTitle(instance.getTitle());
        vo.setBusinessType(instance.getApprovalType());
        vo.setBusinessTypeName(getApprovalTypeName(instance.getApprovalType()));
        vo.setStatus(getStatusStr(instance.getApprovalStatus()));
        vo.setStatusName(getStatusName(instance.getApprovalStatus()));
        vo.setApplicantName(getUserName(instance.getApplicantUserId()));
        vo.setCreatedAt(formatTime(instance.getApplyTime()));
        vo.setFormData(formData);
        vo.setApprovalNodes(approvalNodes);
        vo.setApprovalHistory(approvalHistory);
        vo.setCurrentOperator(currentOperator);
        vo.setCurrentTaskId(currentTaskId);

        return vo;
    }

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

    @Override
    public Integer getPendingCount(Long userId) {
        return Math.toIntExact(taskMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalTaskEntity.class)
                        .eq(ApprovalTaskEntity::getApproverUserId, userId)
                        .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
        ));
    }

    // ========== 内部方法 ==========

    /**
     * 根据筛选条件查询匹配的实例 ID 列表
     */
    private List<Long> queryFilteredInstanceIds(PendingTaskQuery query) {
        LambdaQueryWrapper<ApprovalInstanceEntity> wrapper = Wrappers.lambdaQuery();
        if (org.springframework.util.StringUtils.hasText(query.getBusinessType())) {
            wrapper.eq(ApprovalInstanceEntity::getApprovalType, query.getBusinessType());
        }
        if (org.springframework.util.StringUtils.hasText(query.getKeyword())) {
            wrapper.like(ApprovalInstanceEntity::getTitle, query.getKeyword());
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
                    if (inst == null) return null;
                    return buildTaskVO(task, inst);
                })
                .filter(Objects::nonNull)
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
        vo.setStatus(getStatusStr(instance.getApprovalStatus()));
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
                    vo.setOperatorName(getUserName(task.getApproverUserId()));
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

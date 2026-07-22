package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONUtil;
import com.hrms.business.personnel.common.cache.PersonnelCacheKeys;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.convert.RegularApplicationConvert;
import com.hrms.business.personnel.dto.RegularApplicationApplyRequestDTO;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.common.enums.RegularEvaluateResultEnum;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.RegularApplicationMapper;
import com.hrms.business.personnel.convert.PersonnelDisplayEnricher;
import com.hrms.business.personnel.service.RegularApplicationService;
import com.hrms.business.personnel.vo.RegularApplicationApplyVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hrms.business.personnel.common.constant.RegularApplicationConstant.*;
import static com.hrms.business.personnel.common.enums.ServiceErrorCodeEnum.*;

/**
 * 转正申请服务实现
 */
@Service
@RequiredArgsConstructor
public class RegularApplicationServiceImpl implements RegularApplicationService {


    // 转正申请Mapper
    private final RegularApplicationMapper regularApplicationMapper;
    // 员工快照Mapper
    private final EmployeeSnapshotMapper employeeSnapshotMapper;
    // 员工服务
    private final EmployeeService employeeService;
    // 审批引擎
    private final ApprovalEngine approvalEngine;
    private final ApprovalTaskService approvalTaskService;
    // 部门服务
    private final DeptService deptService;
    // 岗位服务
    private final PostService postService;

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /**
     * 分页查询转正申请。
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页列表
     */
    @Override
    public PageResult<RegularApplicationPageVO> pageRegularApplications(RegularApplicationQueryDTO queryDTO) {
        String queryHash = queryDTO.hashCode() + "_" + queryDTO.getPageNum() + "_" + queryDTO.getPageSize();
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            String cached = rt.opsForValue().get(PersonnelCacheKeys.regularPage(queryHash));
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, PageResult.class);
            }
        }
        if (TAB_EVALUATED.equals(queryDTO.getTab())) {
            return pageEvaluatedRegularApplications(queryDTO);
        }
        return pagePendingRegularEmployees(queryDTO);
    }

    /**
     * 提交转正申请。
     *
     * @param employeeId 员工ID
     * @param requestDTO 转正申请参数
     * @return 转正申请结果
     * 本方法使用的工具类: 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegularApplicationApplyVO applyRegular(Long employeeId, RegularApplicationApplyRequestDTO requestDTO) {
        EmployeeSnapshotEntity employeeSnapshot = getRequiredEmployeeSnapshot(employeeId);
        // 提交防重
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            Boolean locked = rt.opsForValue()
                    .setIfAbsent(PersonnelCacheKeys.regularSubmitToken(employeeId), "1", 30, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new GlobalException(REGULAR_APPLICATION_DUPLICATE, "该申请正在提交中，请勿重复操作");
            }
        }
        // 将从前端传来的 requestDTO.getResult() 字符串值转换为对应的枚举对象。fromValue 方法会遍历所有枚举常量，找到与传入值匹配的枚举项。
        RegularEvaluateResultEnum evaluateResult = RegularEvaluateResultEnum.fromValue(requestDTO.getResult());
        // 校验延长试用时是否填写了延长月数
        if (evaluateResult == RegularEvaluateResultEnum.EXTEND && requestDTO.getExtendMonth() == null) {
            throw new GlobalException(REGULAR_EXTEND_MONTH_REQUIRED);
        }
        // 校验员工没有进行中的转正申请
        assertNoProcessingRegularApplication(employeeId);

        RegularApplicationEntity entity = new RegularApplicationEntity();
        entity.setEmployeeId(employeeId);
        entity.setProbationStartDate(employeeSnapshot.getHireDate());
        entity.setProbationEndDate(employeeSnapshot.getHireDate() == null
                ? null
                : employeeSnapshot.getHireDate().plusMonths(employeeSnapshot.getProbationMonth() == null ? 0 : employeeSnapshot.getProbationMonth()));
        entity.setEvaluateResult(evaluateResult.getCode());
        entity.setExtendMonth(requestDTO.getExtendMonth());
        entity.setSalaryAdjustment(requestDTO.getSalaryAdjustment());
        entity.setEvaluateOpinion(requestDTO.getEvaluateOpinion());
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        regularApplicationMapper.insert(entity);

        //  跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起转正审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.REGULAR.getCode(),
                entity.getId(),
                JSONUtil.toJsonStr(entity),
                SecurityContextHolder.getUserId(),
                employeeSnapshot.getDeptId(),
                employeeId
        );
        entity.setApprovalInstanceId(approvalInstanceId);
        regularApplicationMapper.updateById(entity);

        return RegularApplicationApplyVO.builder()
                .success(Boolean.TRUE)
                .approvalId(approvalInstanceId)
                .build();
    }

    /**
     * 快速审批通过转正申请。
     *
     * @param id 转正申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quickApproveRegularApplication(Long id) {
        RegularApplicationEntity entity = getRequiredRegularApplication(id);
        assertApproving(entity.getApprovalStatus(), "当前转正申请不是审批中状态，无法快速审批");
        processQuickApprove(entity.getApprovalInstanceId(), "当前转正申请无有效审批实例，无法快速审批");
    }

    /**
     * 临时发起转正审批。
     *
     * @param employeeSnapshot 员工快照
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartRegularApproval(EmployeeSnapshotEntity employeeSnapshot) {
    //    return IdUtil.getSnowflakeNextId();
    //}

    /**
     * 查询必须存在的员工快照。
     *
     * @param employeeId 员工ID
     * @return 员工快照
     * 本方法使用的工具类: 无
     */
    private EmployeeSnapshotEntity getRequiredEmployeeSnapshot(Long employeeId) {
        // 跨模块调用已完成：当前调用 EmployeeService#getEmployeeBrief(employeeId) 获取员工简要信息。
        EmployeeEntity employee = employeeService.getEmployeeBrief(employeeId);
        if (employee == null) {
            throw new GlobalException(EMPLOYEE_NOT_FOUND);
        }
        return toEmployeeSnapshot(employee);
    }

    /**
     * 将员工模块实体转换为本模块员工快照。
     *
     * @param employee 员工模块实体
     * @return 本模块员工快照
     * 本方法使用的工具类: 无
     */
    private EmployeeSnapshotEntity toEmployeeSnapshot(EmployeeEntity employee) {
        EmployeeSnapshotEntity snapshot = new EmployeeSnapshotEntity();
        snapshot.setId(employee.getId());
        snapshot.setEmployeeNo(employee.getEmployeeNo());
        snapshot.setEmployeeName(employee.getEmployeeName());
        snapshot.setDeptId(employee.getDeptId());
        snapshot.setPostId(employee.getPostId());
        snapshot.setLeaderId(employee.getLeaderId());
        snapshot.setJobLevel(employee.getJobLevel());
        snapshot.setEmploymentStatus(employee.getEmploymentStatus());
        snapshot.setHireDate(employee.getHireDate());
        snapshot.setProbationMonth(employee.getProbationMonth());
        snapshot.setBaseSalary(employee.getBaseSalary());
        return snapshot;
    }

    /**
     * 校验员工没有进行中的转正申请。
     *
     * @param employeeId 员工ID
     * 本方法使用的工具类: 无
     */
    private void assertNoProcessingRegularApplication(Long employeeId) {
        Long count = regularApplicationMapper.selectCount(new LambdaQueryWrapper<RegularApplicationEntity>()
                .eq(RegularApplicationEntity::getEmployeeId, employeeId)
                .in(RegularApplicationEntity::getApprovalStatus, PENDING_APPROVAL_STATUSES));//替换状态为草稿或审批中
        if (count != null && count > 0) {
            throw new GlobalException(REGULAR_APPLICATION_DUPLICATE);
        }
    }

    /**
     * 查询必定存在的转正申请。
     *
     * @param id 转正申请ID
     * @return 转正申请实体
     */
    private RegularApplicationEntity getRequiredRegularApplication(Long id) {
        RegularApplicationEntity entity = regularApplicationMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "转正申请不存在");
        }
        return entity;
    }

    /**
     * 校验申请是否处于审批中。
     *
     * @param approvalStatus 审批状态
     * @param errorMessage 错误提示
     */
    private void assertApproving(Integer approvalStatus, String errorMessage) {
        if (approvalStatus == null || approvalStatus != ApplicationStatusEnum.APPROVING.getCode()) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, errorMessage);
        }
    }

    /**
     * 通过审批引擎执行快速审批通过。
     *
     * @param approvalInstanceId 审批实例ID
     * @param missingInstanceMessage 审批实例缺失提示
     */
    private void processQuickApprove(Long approvalInstanceId, String missingInstanceMessage) {
        if (approvalInstanceId == null) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, missingInstanceMessage);
        }
        Long pendingTaskId = approvalTaskService.getCurrentPendingTaskIdByInstanceId(approvalInstanceId);
        if (pendingTaskId == null) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "当前审批实例不存在待办审批任务，无法快速审批");
        }
        approvalEngine.processAction(pendingTaskId, "approve", "快速审批通过", null);
    }

    /**
     * 分页查询待转正员工。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private PageResult<RegularApplicationPageVO> pagePendingRegularEmployees(RegularApplicationQueryDTO queryDTO) {
        String queryHash = queryDTO.hashCode() + "_" + queryDTO.getPageNum() + "_" + queryDTO.getPageSize();
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            String cached = rt.opsForValue().get(PersonnelCacheKeys.regularPage(queryHash));
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, PageResult.class);
            }
        }
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        List<EmployeeSnapshotEntity> matchedEmployees = employeeSnapshotMapper.selectList(
                buildPendingEmployeeWrapper(queryDTO)
        );
        Map<Long, RegularApplicationEntity> latestApplicationMap = listLatestRegularApplicationMap(
                matchedEmployees.stream().map(EmployeeSnapshotEntity::getId).toList()
        );
        List<EmployeeSnapshotEntity> eligibleEmployees = matchedEmployees.stream()
                .filter(employee -> !isLatestApplicationEvaluated(latestApplicationMap.get(employee.getId())))
                .toList();
        List<EmployeeSnapshotEntity> pageRecords = paginateEmployees(eligibleEmployees, pageNum, pageSize);
        Map<Long, RegularApplicationEntity> processingApplicationMap = listProcessingRegularApplicationMap(
                pageRecords.stream().map(EmployeeSnapshotEntity::getId).toList()
        );
        List<RegularApplicationPageVO> records = pageRecords.stream()
                // map 方法: 将流中的每个元素（在这里是 EmployeeSnapshotEntity 对象）转换为另一种形式（RegularApplicationPageVO 对象）。
                .map(employee -> RegularApplicationConvert.toPendingVO(
                        employee,
                        processingApplicationMap.get(employee.getId())))
                .map(displayEnricher::enrichRegularApplication)
                .toList();
        return PageResult.of(records, eligibleEmployees.size(), pageNum, pageSize);
    }

    /**
     * 分页查询已评估转正申请。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: CollUtil(hutool)
     */
    private PageResult<RegularApplicationPageVO> pageEvaluatedRegularApplications(RegularApplicationQueryDTO queryDTO) {
        String queryHash = queryDTO.hashCode() + "_" + queryDTO.getPageNum() + "_" + queryDTO.getPageSize();
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            String cached = rt.opsForValue().get(PersonnelCacheKeys.regularPage(queryHash));
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, PageResult.class);
            }
        }
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        // 筛选待转正员工ID列表
        List<Long> targetEmployeeIds = listFilteredEmployeeIds(queryDTO, false);
        // 如果查询条件不为空且待转正员工ID列表为空，则返回空结果
        if ((queryDTO.getDepartmentId() != null || StrUtil.isNotBlank(queryDTO.getKeyword()))
                && CollUtil.isEmpty(targetEmployeeIds)) {
            return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
        }
        // 类型转换,VO增强
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        Page<RegularApplicationEntity> page = regularApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<RegularApplicationEntity>()
                        .in(CollUtil.isNotEmpty(targetEmployeeIds), RegularApplicationEntity::getEmployeeId, targetEmployeeIds)
                        .in(RegularApplicationEntity::getApprovalStatus, EVALUATED_APPROVAL_STATUSES)
                        .orderByDesc(RegularApplicationEntity::getCreateTime)
                        .orderByDesc(RegularApplicationEntity::getId)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(
                page.getRecords().stream().map(RegularApplicationEntity::getEmployeeId).toList()
        );
        List<RegularApplicationPageVO> records = page.getRecords().stream()
                // map 方法: 将流中的每个元素（在这里是 RegularApplicationEntity 对象）转换为另一种形式（RegularApplicationPageVO 对象）。
                .map(entity -> RegularApplicationConvert.toEvaluatedVO(entity, employeeSnapshotMap.get(entity.getEmployeeId())))
                .map(displayEnricher::enrichRegularApplication)
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 构建待转正员工查询条件。
     *
     * @param queryDTO 转正申请查询参数
     * @return 查询条件
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private LambdaQueryWrapper<EmployeeSnapshotEntity> buildPendingEmployeeWrapper(RegularApplicationQueryDTO queryDTO) {
        // 解析部门树范围ID，包含所选部门自身及全部子孙部门。
        List<Long> targetDeptIds = resolveTargetDeptIds(queryDTO.getDepartmentId());

        LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeSnapshotEntity::getEmploymentStatus, EMPLOYMENT_STATUS_PROBATION);
        wrapper.in(CollUtil.isNotEmpty(targetDeptIds), EmployeeSnapshotEntity::getDeptId, targetDeptIds);
        wrapper.and(StrUtil.isNotBlank(queryDTO.getKeyword()), keywordWrapper -> keywordWrapper
                .like(EmployeeSnapshotEntity::getEmployeeName, queryDTO.getKeyword())
                .or()
                .like(EmployeeSnapshotEntity::getEmployeeNo, queryDTO.getKeyword()));
        wrapper.orderByAsc(EmployeeSnapshotEntity::getHireDate);
        return wrapper;
    }

    /**
     * 批量查询员工快照映射。
     *
     * @param employeeIds 员工ID列表
     * @return 员工快照映射
     * 本方法使用的工具类: CollUtil(hutool)
     */
    private Map<Long, EmployeeSnapshotEntity> listEmployeeSnapshotMap(List<Long> employeeIds) {
        // 如果员工ID列表为空，则返回空映射
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        //  跨模块调用已完成：当前员工模块暂无批量快照接口，暂用 EmployeeService#getEmployeeBrief(employeeId) 循环补全。
        return employeeIds.stream()
                .map(employeeService::getEmployeeBrief)
                .filter(employee -> employee != null)
                .map(this::toEmployeeSnapshot)
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 批量查询员工进行中的转正申请映射。
     *
     * @param employeeIds 员工ID列表
     * @return 进行中的转正申请映射
     * 本方法使用的工具类: CollUtil(hutool),Collectors(JDK)
     */
    private Map<Long, RegularApplicationEntity> listProcessingRegularApplicationMap(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        List<RegularApplicationEntity> applications = regularApplicationMapper.selectList(
                new LambdaQueryWrapper<RegularApplicationEntity>()
                        .in(RegularApplicationEntity::getEmployeeId, employeeIds)
                        .in(RegularApplicationEntity::getApprovalStatus, PENDING_APPROVAL_STATUSES)
                        .orderByDesc(RegularApplicationEntity::getCreateTime)
                        .orderByDesc(RegularApplicationEntity::getId)
        );
        return applications.stream()
                .collect(Collectors.toMap(
                        RegularApplicationEntity::getEmployeeId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    /**
     * 批量查询员工最新一条转正申请映射。
     *
     * @param employeeIds 员工ID列表
     * @return 员工最新转正申请映射
     * 本方法使用的工具类: CollUtil(hutool),Collectors(JDK)
     */
    private Map<Long, RegularApplicationEntity> listLatestRegularApplicationMap(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        List<RegularApplicationEntity> applications = regularApplicationMapper.selectList(
                new LambdaQueryWrapper<RegularApplicationEntity>()
                        .in(RegularApplicationEntity::getEmployeeId, employeeIds)
                        .orderByDesc(RegularApplicationEntity::getCreateTime)
                        .orderByDesc(RegularApplicationEntity::getId)
        );
        return applications.stream()
                .collect(Collectors.toMap(
                        RegularApplicationEntity::getEmployeeId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    /**
     * 判断员工最新一条转正申请是否已经进入已评估范围。
     *
     * @param latestApplication 员工最新转正申请
     * @return 是否已评估
     * 本方法使用的工具类: 无
     */
    private boolean isLatestApplicationEvaluated(RegularApplicationEntity latestApplication) {
        return latestApplication != null
                && EVALUATED_APPROVAL_STATUSES.contains(latestApplication.getApprovalStatus());
    }

    /**
     * 对员工列表做内存分页。
     *
     * @param employees 员工列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 当前页员工列表
     * 本方法使用的工具类: Math(JDK),CollUtil(hutool)
     */
    private List<EmployeeSnapshotEntity> paginateEmployees(List<EmployeeSnapshotEntity> employees, int pageNum, int pageSize) {
        if (CollUtil.isEmpty(employees)) {
            return Collections.emptyList();
        }
        int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
        if (fromIndex >= employees.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, employees.size());
        return employees.subList(fromIndex, toIndex);
    }

    /**
     * 解析部门树范围内的员工ID列表。
     *
     * @param queryDTO 查询参数
     * @param probationOnly 是否只筛选试用期员工
     * @return 员工ID列表
     */
    private List<Long> listFilteredEmployeeIds(RegularApplicationQueryDTO queryDTO, boolean probationOnly) {
        LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper = new LambdaQueryWrapper<>();
        // 只筛选试用期员工
        if (probationOnly) {
            wrapper.eq(EmployeeSnapshotEntity::getEmploymentStatus, EMPLOYMENT_STATUS_PROBATION);
        }
        // 解析部门树范围ID，包含所选部门自身及全部子孙部门。
        List<Long> targetDeptIds = resolveTargetDeptIds(queryDTO.getDepartmentId());
        //筛选部门树范围
        wrapper.in(CollUtil.isNotEmpty(targetDeptIds), EmployeeSnapshotEntity::getDeptId, targetDeptIds);
        wrapper.and(StrUtil.isNotBlank(queryDTO.getKeyword()), keywordWrapper -> keywordWrapper
                .like(EmployeeSnapshotEntity::getEmployeeName, queryDTO.getKeyword())
                .or()
                .like(EmployeeSnapshotEntity::getEmployeeNo, queryDTO.getKeyword()));
        return employeeSnapshotMapper.selectList(wrapper).stream()
                .map(EmployeeSnapshotEntity::getId)
                .toList();
    }

    /**
     * 解析部门树范围ID，包含所选部门自身及全部子孙部门。
     *
     * @param departmentId 所选部门ID
     * @return 部门树范围ID列表
     */
    private List<Long> resolveTargetDeptIds(Long departmentId) {
        if (departmentId == null) {
            return Collections.emptyList();
        }
        List<Long> deptIds = deptService.getSubDeptIds(departmentId);
        if (CollUtil.isEmpty(deptIds)) {
            return List.of(departmentId);
        }
        return new ArrayList<>(Set.copyOf(deptIds));
    }

    /**
     * 规范化页码。
     *
     * @param pageNum 页码
     * @return 有效页码
     * 本方法使用的工具类: 无
     */
    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 规范化每页条数。
     *
     * @param pageSize 每页条数
     * @return 有效每页条数
     * 本方法使用的工具类: Math(JDK)
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

}

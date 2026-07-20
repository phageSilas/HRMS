package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONUtil;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.convert.RegularApplicationConvert;
import com.hrms.business.personnel.dto.RegularApplicationApplyRequestDTO;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.enums.RegularEvaluateResultEnum;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转正申请服务实现
 */
@Service
@RequiredArgsConstructor
public class RegularApplicationServiceImpl implements RegularApplicationService {

    private static final ErrorCode EMPLOYEE_NOT_FOUND = new ErrorCode(40060, "员工不存在");

    private static final ErrorCode REGULAR_APPLICATION_DUPLICATE = new ErrorCode(40061, "员工已有进行中的转正申请");

    private static final ErrorCode REGULAR_EXTEND_MONTH_REQUIRED = new ErrorCode(40062, "延长试用时必须填写延长月数");

    private static final String TAB_EVALUATED = "evaluated";

    private static final int EMPLOYMENT_STATUS_PROBATION = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    private final RegularApplicationMapper regularApplicationMapper;

    private final EmployeeSnapshotMapper employeeSnapshotMapper;

    private final EmployeeService employeeService;

    private final ApprovalEngine approvalEngine;

    private final DeptService deptService;

    private final PostService postService;

    /**
     * 分页查询转正申请。
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页列表
     */
    @Override
    public PageResult<RegularApplicationPageVO> pageRegularApplications(RegularApplicationQueryDTO queryDTO) {
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
        // 将从前端传来的 requestDTO.getResult() 字符串值转换为对应的枚举对象。fromValue 方法会遍历所有枚举常量，找到与传入值匹配的枚举项。
        RegularEvaluateResultEnum evaluateResult = RegularEvaluateResultEnum.fromValue(requestDTO.getResult());
        // 校验延长试用时是否填写了延长月数
        if (evaluateResult == RegularEvaluateResultEnum.EXTEND && requestDTO.getExtendMonth() == null) {
            throw new GlobalException(REGULAR_EXTEND_MONTH_REQUIRED);
        }
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
                .in(RegularApplicationEntity::getApprovalStatus,
                        ApplicationStatusEnum.DRAFT.getCode(), ApplicationStatusEnum.APPROVING.getCode()));//替换状态为草稿或审批中
        if (count != null && count > 0) {
            throw new GlobalException(REGULAR_APPLICATION_DUPLICATE);
        }
    }

    /**
     * 分页查询待转正员工。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private PageResult<RegularApplicationPageVO> pagePendingRegularEmployees(RegularApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        Page<EmployeeSnapshotEntity> page = employeeSnapshotMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildPendingEmployeeWrapper(queryDTO)
        );
        Map<Long, RegularApplicationEntity> processingApplicationMap = listProcessingRegularApplicationMap(
                page.getRecords().stream().map(EmployeeSnapshotEntity::getId).toList()
        );
        List<RegularApplicationPageVO> records = page.getRecords().stream()
                // map 方法: 将流中的每个元素（在这里是 EmployeeSnapshotEntity 对象）转换为另一种形式（RegularApplicationPageVO 对象）。
                .map(employee -> RegularApplicationConvert.toPendingVO(
                        employee,
                        processingApplicationMap.get(employee.getId())))
                .map(displayEnricher::enrichRegularApplication)
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 分页查询已评估转正申请。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: CollUtil(hutool)
     */
    private PageResult<RegularApplicationPageVO> pageEvaluatedRegularApplications(RegularApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        Page<RegularApplicationEntity> page = regularApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<RegularApplicationEntity>().orderByDesc(RegularApplicationEntity::getCreateTime)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(
                page.getRecords().stream().map(RegularApplicationEntity::getEmployeeId).toList()
        );
        List<RegularApplicationPageVO> records = page.getRecords().stream()
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
        LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeSnapshotEntity::getEmploymentStatus, EMPLOYMENT_STATUS_PROBATION);
        wrapper.eq(queryDTO.getDepartmentId() != null, EmployeeSnapshotEntity::getDeptId, queryDTO.getDepartmentId());
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
                        .in(RegularApplicationEntity::getApprovalStatus,
                                ApplicationStatusEnum.DRAFT.getCode(),
                                ApplicationStatusEnum.APPROVING.getCode())
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

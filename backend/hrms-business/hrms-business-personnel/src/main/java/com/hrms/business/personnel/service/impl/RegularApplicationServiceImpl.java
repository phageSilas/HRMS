package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.personnel.convert.RegularApplicationConvert;
import com.hrms.business.personnel.dto.RegularApplicationApplyRequestDTO;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.enums.RegularEvaluateResultEnum;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.RegularApplicationMapper;
import com.hrms.business.personnel.service.RegularApplicationService;
import com.hrms.business.personnel.vo.RegularApplicationApplyVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
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

    @Override
    public PageResult<RegularApplicationPageVO> pageRegularApplications(RegularApplicationQueryDTO queryDTO) {
        if (TAB_EVALUATED.equals(queryDTO.getTab())) {
            return pageEvaluatedRegularApplications(queryDTO);
        }
        return pagePendingRegularEmployees(queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegularApplicationApplyVO applyRegular(Long employeeId, RegularApplicationApplyRequestDTO requestDTO) {
        EmployeeSnapshotEntity employeeSnapshot = getRequiredEmployeeSnapshot(employeeId);
        RegularEvaluateResultEnum evaluateResult = RegularEvaluateResultEnum.fromValue(requestDTO.getResult());
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
        // approvalService.startApproval("REGULAR", entity.getId()); 本接口需要调用 hrms-business-approval 模块的转正审批发起接口
        Long approvalInstanceId = tempStartRegularApproval(employeeSnapshot);
        entity.setApprovalInstanceId(approvalInstanceId);
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        regularApplicationMapper.insert(entity);

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
    private Long tempStartRegularApproval(EmployeeSnapshotEntity employeeSnapshot) {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 查询必须存在的员工快照。
     *
     * @param employeeId 员工ID
     * @return 员工快照
     * 本方法使用的工具类: 无
     */
    private EmployeeSnapshotEntity getRequiredEmployeeSnapshot(Long employeeId) {
        // employeeService.getEmployeeSnapshot(employeeId); 本接口需要调用 hrms-business-employee 模块的员工快照详情接口
        EmployeeSnapshotEntity employeeSnapshot = employeeSnapshotMapper.selectById(employeeId);
        if (employeeSnapshot == null) {
            throw new GlobalException(EMPLOYEE_NOT_FOUND);
        }
        return employeeSnapshot;
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
                        ApplicationStatusEnum.DRAFT.getCode(), ApplicationStatusEnum.APPROVING.getCode()));
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
        Page<EmployeeSnapshotEntity> page = employeeSnapshotMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildPendingEmployeeWrapper(queryDTO)
        );
        List<RegularApplicationPageVO> records = page.getRecords().stream()
                .map(RegularApplicationConvert::toPendingVO)
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
        Page<RegularApplicationEntity> page = regularApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<RegularApplicationEntity>().orderByDesc(RegularApplicationEntity::getCreateTime)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(
                page.getRecords().stream().map(RegularApplicationEntity::getEmployeeId).toList()
        );
        List<RegularApplicationPageVO> records = page.getRecords().stream()
                .map(entity -> RegularApplicationConvert.toEvaluatedVO(entity, employeeSnapshotMap.get(entity.getEmployeeId())))
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
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        // employeeService.listEmployeeSnapshots(employeeIds); 本接口需要调用 hrms-business-employee 模块的员工快照批量查询接口
        return employeeSnapshotMapper.selectBatchIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, Function.identity(), (left, right) -> left));
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

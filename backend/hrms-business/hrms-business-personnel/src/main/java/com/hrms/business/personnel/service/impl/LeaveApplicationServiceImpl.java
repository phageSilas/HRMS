package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.employee.dto.EmployeeQueryDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.employee.vo.EmployeeListVO;
import com.hrms.business.personnel.convert.LeaveApplicationConvert;
import com.hrms.business.personnel.dto.LeaveApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.LeaveApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.enums.LeaveTypeEnum;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.LeaveApplicationMapper;
import com.hrms.business.personnel.service.LeaveApplicationService;
import com.hrms.business.personnel.vo.LeaveApplicationCreateVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 离职申请服务实现
 */
@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private static final ErrorCode EMPLOYEE_NOT_FOUND = new ErrorCode(40060, "员工不存在");

    private static final ErrorCode LEAVE_APPLICATION_DUPLICATE = new ErrorCode(40081, "员工已有进行中的离职申请");

    private static final Long IMPOSSIBLE_EMPLOYEE_ID = -1L;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    private final LeaveApplicationMapper leaveApplicationMapper;

    private final EmployeeSnapshotMapper employeeSnapshotMapper;

    private final EmployeeService employeeService;

    private final ApprovalEngine approvalEngine;

    /**
     * 离职申请分页查询
     * @param queryDTO 离职申请查询参数
     * @return 离职申请分页列表
     */
    @Override
    public PageResult<LeaveApplicationPageVO> pageLeaveApplications(LeaveApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        Page<LeaveApplicationEntity> page = leaveApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildLeaveApplicationWrapper(queryDTO)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(collectEmployeeIds(page.getRecords()));
        List<LeaveApplicationPageVO> records = page.getRecords().stream()
                .map(entity -> LeaveApplicationConvert.toPageVO(
                        entity,
                        employeeSnapshotMap.get(entity.getEmployeeId()),
                        employeeSnapshotMap.get(entity.getHandoverEmployeeId())))
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 创建离职申请
     * @param requestDTO 离职申请创建参数
     * @return 离职申请创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveApplicationCreateVO createLeaveApplication(LeaveApplicationCreateRequestDTO requestDTO) {
        EmployeeSnapshotEntity employeeSnapshot = getRequiredEmployeeSnapshot(requestDTO.getEmployeeId());
        getRequiredEmployeeSnapshot(requestDTO.getHandoverEmployeeId());
        assertNoProcessingLeaveApplication(requestDTO.getEmployeeId());

        LeaveApplicationEntity entity = new LeaveApplicationEntity();
        entity.setEmployeeId(employeeSnapshot.getId());
        entity.setLeaveType(LeaveTypeEnum.fromValue(requestDTO.getLeaveType()).getCode());
        entity.setLeaveReason(requestDTO.getLeaveReason());
        entity.setApplyDate(java.time.LocalDate.now());
        entity.setExpectedLastWorkDate(requestDTO.getLastWorkDate());
        entity.setHandoverEmployeeId(requestDTO.getHandoverEmployeeId());
        entity.setHandoverStatus(0);
        entity.setRemark(requestDTO.getRemark());
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        leaveApplicationMapper.insert(entity);

        // TODO 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起离职审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.LEAVE.getCode(),
                entity.getId(),
                JSONUtil.toJsonStr(entity),
                SecurityContextHolder.getUserId(),
                employeeSnapshot.getDeptId(),
                employeeSnapshot.getId()
        );
        entity.setApprovalInstanceId(approvalInstanceId);
        leaveApplicationMapper.updateById(entity);

        return LeaveApplicationCreateVO.builder()
                .id(entity.getId())
                .build();
    }

    /**
     * 临时发起离职审批。
     *
     * @param employeeSnapshot 员工快照
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartLeaveApproval(EmployeeSnapshotEntity employeeSnapshot) {
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
        // TODO 跨模块调用已完成：当前调用 EmployeeService#getEmployeeBrief(employeeId) 获取员工简要信息。
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
     * 校验员工没有进行中的离职申请。
     *
     * @param employeeId 员工ID
     * 本方法使用的工具类: 无
     */
    private void assertNoProcessingLeaveApplication(Long employeeId) {
        Long count = leaveApplicationMapper.selectCount(new LambdaQueryWrapper<LeaveApplicationEntity>()
                .eq(LeaveApplicationEntity::getEmployeeId, employeeId)
                .in(LeaveApplicationEntity::getApprovalStatus,
                        ApplicationStatusEnum.DRAFT.getCode(), ApplicationStatusEnum.APPROVING.getCode()));
        if (count != null && count > 0) {
            throw new GlobalException(LEAVE_APPLICATION_DUPLICATE);
        }
    }

    /**
     * 构建离职申请分页查询条件。
     *
     * @param queryDTO 离职申请查询参数
     * @return 查询条件
     * 本方法使用的工具类: StrUtil(hutool),CollUtil(hutool)
     */
    private LambdaQueryWrapper<LeaveApplicationEntity> buildLeaveApplicationWrapper(LeaveApplicationQueryDTO queryDTO) {
        LambdaQueryWrapper<LeaveApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getLeaveType())) {
            wrapper.eq(LeaveApplicationEntity::getLeaveType, LeaveTypeEnum.fromValue(queryDTO.getLeaveType()).getCode());
        }
        wrapper.eq(queryDTO.getApprovalStatus() != null, LeaveApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        if (queryDTO.getDepartmentId() != null || StrUtil.isNotBlank(queryDTO.getKeyword())) {
            List<Long> employeeIds = listEmployeeIdsByQuery(queryDTO);
            wrapper.in(CollUtil.isNotEmpty(employeeIds), LeaveApplicationEntity::getEmployeeId, employeeIds);
            wrapper.eq(CollUtil.isEmpty(employeeIds), LeaveApplicationEntity::getEmployeeId, IMPOSSIBLE_EMPLOYEE_ID);
        }
        wrapper.orderByDesc(LeaveApplicationEntity::getCreateTime);
        return wrapper;
    }

    /**
     * 根据查询条件获取员工ID列表。
     *
     * @param queryDTO 离职申请查询参数
     * @return 员工ID列表
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private List<Long> listEmployeeIdsByQuery(LeaveApplicationQueryDTO queryDTO) {
        // TODO 跨模块调用已完成：当前调用 EmployeeService#listEmployees(employeeQueryDTO) 按部门和关键词查询员工列表。
        EmployeeQueryDTO employeeQueryDTO = new EmployeeQueryDTO();
        employeeQueryDTO.setKeyword(queryDTO.getKeyword());
        employeeQueryDTO.setDeptIds(queryDTO.getDepartmentId() == null ? null : List.of(queryDTO.getDepartmentId()));
        employeeQueryDTO.setPageNum(1);
        employeeQueryDTO.setPageSize(MAX_PAGE_SIZE);
        return employeeService.listEmployees(employeeQueryDTO).getRecords().stream()
                .map(EmployeeListVO::getId)
                .filter(id -> id != null)
                .toList();
    }

    /**
     * 收集离职申请涉及的员工ID。
     *
     * @param records 离职申请列表
     * @return 员工ID列表
     * 本方法使用的工具类: Stream(JDK)
     */
    private List<Long> collectEmployeeIds(List<LeaveApplicationEntity> records) {
        return records.stream()
                .flatMap(entity -> Stream.of(entity.getEmployeeId(), entity.getHandoverEmployeeId()))
                .filter(id -> id != null)
                .distinct()
                .toList();
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
        // TODO 跨模块调用已完成：当前员工模块暂无批量快照接口，暂用 EmployeeService#getEmployeeBrief(employeeId) 循环补全。
        return employeeIds.stream()
                .map(employeeService::getEmployeeBrief)
                .filter(employee -> employee != null)
                .map(this::toEmployeeSnapshot)
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

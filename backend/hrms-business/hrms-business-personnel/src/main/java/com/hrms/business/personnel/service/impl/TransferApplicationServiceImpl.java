package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import com.hrms.business.personnel.convert.TransferApplicationConvert;
import com.hrms.business.personnel.dto.TransferApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.TransferApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.TransferApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.TransferApplicationMapper;
import com.hrms.business.personnel.convert.PersonnelDisplayEnricher;
import com.hrms.business.personnel.service.TransferApplicationService;
import com.hrms.business.personnel.vo.TransferApplicationCreateVO;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hrms.business.personnel.common.constant.TransferApplicationConstant.*;
import static com.hrms.business.personnel.common.enums.ServiceErrorCodeEnum.EMPLOYEE_NOT_FOUND;
import static com.hrms.business.personnel.common.enums.ServiceErrorCodeEnum.TRANSFER_APPLICATION_DUPLICATE;

/**
 * 调岗申请服务实现
 */
@Service
@RequiredArgsConstructor
public class TransferApplicationServiceImpl implements TransferApplicationService {
    // 调岗申请Mapper
    private final TransferApplicationMapper transferApplicationMapper;
    // 员工服务
    private final EmployeeService employeeService;
    // 审批引擎
    private final ApprovalEngine approvalEngine;
    // 部门服务
    private final DeptService deptService;
    // 岗位服务
    private final PostService postService;

    /**
     * 分页查询调岗申请。
     * @param queryDTO 调岗申请查询参数
      * @return 调岗申请分页列表
     */
    @Override
    public PageResult<TransferApplicationPageVO> pageTransferApplications(TransferApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        // 类型转换
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);

        Page<TransferApplicationEntity> page = transferApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildTransferApplicationWrapper(queryDTO)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(
                page.getRecords().stream().map(TransferApplicationEntity::getEmployeeId).toList()
        );
        List<TransferApplicationPageVO> records = page.getRecords().stream()
                // 转换为调岗申请分页列表
                .map(entity -> displayEnricher.enrichTransferApplication(
                        TransferApplicationConvert.toPageVO(entity, employeeSnapshotMap.get(entity.getEmployeeId())),
                        entity.getFromDeptId(),
                        entity.getFromPostId(),
                        entity.getToDeptId(),
                        entity.getToPostId()))
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 创建调岗申请。
     *
     * @param requestDTO 调岗申请创建参数
     * @return 调岗申请创建结果
     * 本方法使用的工具类: IdUtil(hutool)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferApplicationCreateVO createTransferApplication(TransferApplicationCreateRequestDTO requestDTO) {
        //查询必须存在的员工快照。
        EmployeeSnapshotEntity employeeSnapshot = getRequiredEmployeeSnapshot(requestDTO.getEmployeeId());
        // 确保员工没有进行中的调岗申请
        assertNoProcessingTransferApplication(requestDTO.getEmployeeId());

        TransferApplicationEntity entity = new TransferApplicationEntity();
        entity.setEmployeeId(requestDTO.getEmployeeId());
        entity.setFromDeptId(employeeSnapshot.getDeptId());
        entity.setToDeptId(requestDTO.getToDeptId());
        entity.setFromPostId(employeeSnapshot.getPostId());
        entity.setToPostId(requestDTO.getToPostId());
        entity.setFromJobLevel(employeeSnapshot.getJobLevel());
        entity.setToJobLevel(requestDTO.getToJobLevel());
        entity.setFromLeaderId(employeeSnapshot.getLeaderId());
        entity.setToLeaderId(requestDTO.getToLeaderId());
        entity.setSalaryAdjustment(requestDTO.getSalaryAdjustment());
        entity.setEffectiveDate(requestDTO.getEffectiveDate());
        entity.setReason(requestDTO.getReason());
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        transferApplicationMapper.insert(entity);

        //  跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起调岗审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.TRANSFER.getCode(),
                entity.getId(),
                JSONUtil.toJsonStr(entity),
                SecurityContextHolder.getUserId(),
                employeeSnapshot.getDeptId(),
                requestDTO.getEmployeeId()
        );
        // 更新调岗申请的审批实例ID
        entity.setApprovalInstanceId(approvalInstanceId);
        transferApplicationMapper.updateById(entity);

        return TransferApplicationCreateVO.builder()
                .id(entity.getId())
                .approvalStatus(entity.getApprovalStatus())
                .build();
    }

    /**
     * 临时发起调岗审批。
     *
     * @param employeeSnapshot 员工快照
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartTransferApproval(EmployeeSnapshotEntity employeeSnapshot) {
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
     * 校验员工没有进行中的调岗申请。
     *
     * @param employeeId 员工ID
     * 本方法使用的工具类: 无
     */
    private void assertNoProcessingTransferApplication(Long employeeId) {
        Long count = transferApplicationMapper.selectCount(new LambdaQueryWrapper<TransferApplicationEntity>()
                .eq(TransferApplicationEntity::getEmployeeId, employeeId)
                .in(TransferApplicationEntity::getApprovalStatus,
                        ApplicationStatusEnum.DRAFT.getCode(), ApplicationStatusEnum.APPROVING.getCode()));
        if (count != null && count > 0) {
            throw new GlobalException(TRANSFER_APPLICATION_DUPLICATE);
        }
    }

    /**
     * 构建调岗申请分页查询条件。
     *
     * @param queryDTO 调岗申请查询参数
     * @return 查询条件
     * 本方法使用的工具类: 无
     */
    private LambdaQueryWrapper<TransferApplicationEntity> buildTransferApplicationWrapper(TransferApplicationQueryDTO queryDTO) {
        //解析部门树范围ID，包含所选部门自身及全部子孙部门
        List<Long> targetDeptIds = resolveTargetDeptIds(queryDTO.getDepartmentId());
        LambdaQueryWrapper<TransferApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        //筛选部门树范围
        wrapper.in(CollUtil.isNotEmpty(targetDeptIds), TransferApplicationEntity::getFromDeptId, targetDeptIds);
        //筛选审批状态
        wrapper.eq(queryDTO.getApprovalStatus() != null, TransferApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            List<Long> employeeIds = listEmployeeIdsByKeyword(queryDTO.getKeyword());
            //筛选员工ID
            wrapper.in(CollUtil.isNotEmpty(employeeIds), TransferApplicationEntity::getEmployeeId, employeeIds);
            //如果无匹配员工，则筛选无员工ID
            wrapper.eq(CollUtil.isEmpty(employeeIds), TransferApplicationEntity::getEmployeeId, IMPOSSIBLE_EMPLOYEE_ID);
        }
        //按创建时间降序排序
        wrapper.orderByDesc(TransferApplicationEntity::getCreateTime);
        return wrapper;
    }

    /**
     * 解析部门树范围ID，包含所选部门自身及全部子孙部门。
     *
     * @param departmentId 所选部门ID
     * @return 部门树范围ID列表
     */
    private List<Long> resolveTargetDeptIds(Long departmentId) {
        if (departmentId == null) {
            return List.of();
        }
        List<Long> deptIds = deptService.getSubDeptIds(departmentId);
        if (CollUtil.isEmpty(deptIds)) {
            return List.of(departmentId);
        }
        return new ArrayList<>(Set.copyOf(deptIds));
    }

    /**
     * 根据关键词查询员工ID列表。
     *
     * @param keyword 关键词
     * @return 员工ID列表
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private List<Long> listEmployeeIdsByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        //  跨模块调用已完成：当前调用 EmployeeService#listEmployees(queryDTO) 按关键词查询员工列表。
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(MAX_PAGE_SIZE);
        return employeeService.listEmployees(queryDTO).getRecords().stream()
                .map(EmployeeListVO::getId)
                .filter(Objects::nonNull)
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
        //
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        //  跨模块调用已完成：当前员工模块暂无批量快照接口，暂用 EmployeeService#getEmployeeBrief(employeeId) 循环补全。
        return employeeIds.stream()
                .map(employeeService::getEmployeeBrief)
                .filter(Objects::nonNull)
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

package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.employee.dto.ContractCreateDTO;
import com.hrms.business.employee.dto.ContractUpdateDTO;
import com.hrms.business.employee.entity.EmployeeContractEntity;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.enums.ContractTypeEnum;
import com.hrms.business.employee.mapper.EmployeeContractMapper;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.business.employee.service.EmployeeContractService;
import com.hrms.business.employee.vo.EmployeeContractDetailVO;
import com.hrms.business.employee.vo.EmployeeContractVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工合同服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeContractServiceImpl implements EmployeeContractService {

    private final EmployeeContractMapper contractMapper;
    private final EmployeeMapper employeeMapper;
    private final DeptService deptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeContractEntity createContract(ContractCreateDTO createDTO) {
        // 校验日期逻辑
        validateContractDates(createDTO.getStartDate(), createDTO.getEndDate());

        EmployeeContractEntity entity = new EmployeeContractEntity();
        entity.setEmployeeId(createDTO.getEmployeeId());
        entity.setContractNo(createDTO.getContractNo());
        entity.setContractType(createDTO.getContractType());
        entity.setStartDate(createDTO.getStartDate());
        entity.setEndDate(createDTO.getEndDate());
        entity.setProbationMonth(createDTO.getProbationMonth());
        entity.setProbationSalaryRatio(createDTO.getProbationSalaryRatio());
        entity.setAttachmentFileId(createDTO.getAttachmentFileId());
        entity.setSigningCount(1); // 首次签订
        entity.setRemark(createDTO.getRemark());

        contractMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeContractEntity updateContract(Long id, ContractUpdateDTO updateDTO) {
        EmployeeContractEntity entity = contractMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "合同不存在");
        }

        if (updateDTO.getContractNo() != null) {
            entity.setContractNo(updateDTO.getContractNo());
        }
        if (updateDTO.getContractType() != null) {
            entity.setContractType(updateDTO.getContractType());
        }
        if (updateDTO.getStartDate() != null) {
            entity.setStartDate(updateDTO.getStartDate());
        }
        if (updateDTO.getEndDate() != null) {
            entity.setEndDate(updateDTO.getEndDate());
        }
        if (updateDTO.getProbationMonth() != null) {
            entity.setProbationMonth(updateDTO.getProbationMonth());
        }
        if (updateDTO.getProbationSalaryRatio() != null) {
            entity.setProbationSalaryRatio(updateDTO.getProbationSalaryRatio());
        }
        if (updateDTO.getAttachmentFileId() != null) {
            entity.setAttachmentFileId(updateDTO.getAttachmentFileId());
        }
        if (updateDTO.getRemark() != null) {
            entity.setRemark(updateDTO.getRemark());
        }

        // 校验日期逻辑
        validateContractDates(entity.getStartDate(), entity.getEndDate());

        contractMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContract(Long id) {
        EmployeeContractEntity entity = contractMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        contractMapper.deleteById(id);
    }

    @Override
    public EmployeeContractVO getContractDetail(Long id) {
        EmployeeContractEntity entity = contractMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        return convertToVO(entity);
    }

    @Override
    public List<EmployeeContractVO> getContractsByEmployee(Long employeeId) {
        LambdaQueryWrapper<EmployeeContractEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(EmployeeContractEntity::getEmployeeId, employeeId);
        wrapper.orderByDesc(EmployeeContractEntity::getCreateTime);
        List<EmployeeContractEntity> list = contractMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public EmployeeContractEntity getCurrentContract(Long employeeId) {
        LambdaQueryWrapper<EmployeeContractEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(EmployeeContractEntity::getEmployeeId, employeeId);
        wrapper.le(EmployeeContractEntity::getStartDate, LocalDate.now());
        wrapper.ge(EmployeeContractEntity::getEndDate, LocalDate.now());
        wrapper.orderByDesc(EmployeeContractEntity::getStartDate);
        wrapper.last("LIMIT 1");
        return contractMapper.selectOne(wrapper);
    }

    /**
     * 转换为 VO
     */
    private EmployeeContractVO convertToVO(EmployeeContractEntity entity) {
        EmployeeContractVO vo = new EmployeeContractVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setContractNo(entity.getContractNo());
        vo.setContractType(entity.getContractType());
        vo.setContractTypeDesc(getContractTypeDesc(entity.getContractType()));
        vo.setStartDate(entity.getStartDate());
        vo.setEndDate(entity.getEndDate());
        vo.setProbationMonth(entity.getProbationMonth());
        vo.setProbationSalaryRatio(entity.getProbationSalaryRatio());
        vo.setAttachmentFileId(entity.getAttachmentFileId());
        vo.setSigningCount(entity.getSigningCount());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 获取合同类型描述
     */
    private String getContractTypeDesc(Integer contractType) {
        ContractTypeEnum enumValue = ContractTypeEnum.fromCode(contractType);
        return enumValue != null ? enumValue.getDesc() : "未知";
    }

    /**
     * 校验合同日期逻辑
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     */
    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (!endDate.isAfter(startDate)) {
                throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "合同结束日期必须晚于开始日期");
            }
        }
    }

    @Override
    public List<EmployeeContractDetailVO> getAllContracts(Integer pageNum, Integer pageSize, String keyword) {
        Page<EmployeeContractEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EmployeeContractEntity> wrapper = Wrappers.lambdaQuery();

        // 如果有关键词，需要先查找匹配的员工
        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<EmployeeEntity> empWrapper = Wrappers.lambdaQuery();
            empWrapper.and(w -> w
                    .like(EmployeeEntity::getEmployeeName, keyword)
                    .or()
                    .like(EmployeeEntity::getEmployeeNo, keyword)
            );
            List<EmployeeEntity> employees = employeeMapper.selectList(empWrapper);
            if (!employees.isEmpty()) {
                List<Long> employeeIds = employees.stream()
                        .map(EmployeeEntity::getId)
                        .collect(Collectors.toList());
                wrapper.in(EmployeeContractEntity::getEmployeeId, employeeIds);
            } else {
                // 如果没有匹配的员工，也按合同编号搜索
                wrapper.and(w -> w.like(EmployeeContractEntity::getContractNo, keyword));
            }
        }

        wrapper.orderByDesc(EmployeeContractEntity::getCreateTime);
        Page<EmployeeContractEntity> result = contractMapper.selectPage(page, wrapper);

        return convertToDetailVOList(result.getRecords());
    }

    @Override
    public long getAllContractsCount(String keyword) {
        LambdaQueryWrapper<EmployeeContractEntity> wrapper = Wrappers.lambdaQuery();

        // 如果有关键词，需要先查找匹配的员工
        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<EmployeeEntity> empWrapper = Wrappers.lambdaQuery();
            empWrapper.and(w -> w
                    .like(EmployeeEntity::getEmployeeName, keyword)
                    .or()
                    .like(EmployeeEntity::getEmployeeNo, keyword)
            );
            List<EmployeeEntity> employees = employeeMapper.selectList(empWrapper);
            if (!employees.isEmpty()) {
                List<Long> employeeIds = employees.stream()
                        .map(EmployeeEntity::getId)
                        .collect(Collectors.toList());
                wrapper.in(EmployeeContractEntity::getEmployeeId, employeeIds);
            } else {
                // 如果没有匹配的员工，也按合同编号搜索
                wrapper.and(w -> w.like(EmployeeContractEntity::getContractNo, keyword));
            }
        }

        return contractMapper.selectCount(wrapper);
    }

    /**
     * 批量转换为详情VO（包含员工信息）
     */
    private List<EmployeeContractDetailVO> convertToDetailVOList(List<EmployeeContractEntity> contracts) {
        if (contracts == null || contracts.isEmpty()) {
            return List.of();
        }

        // 获取所有员工ID
        List<Long> employeeIds = contracts.stream()
                .map(EmployeeContractEntity::getEmployeeId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询员工信息
        LambdaQueryWrapper<EmployeeEntity> empWrapper = Wrappers.lambdaQuery();
        empWrapper.in(EmployeeEntity::getId, employeeIds);
        List<EmployeeEntity> employees = employeeMapper.selectList(empWrapper);
        Map<Long, EmployeeEntity> employeeMap = employees.stream()
                .collect(Collectors.toMap(EmployeeEntity::getId, e -> e));

        // 获取所有部门ID
        List<Long> deptIds = employees.stream()
                .map(EmployeeEntity::getDeptId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询部门信息
        Map<Long, String> deptNameMap = new java.util.HashMap<>();
        for (Long deptId : deptIds) {
            try {
                DeptDetailVO dept = deptService.getDeptById(deptId);
                if (dept != null) {
                    deptNameMap.put(deptId, dept.getDeptName());
                }
            } catch (Exception e) {
                log.warn("获取部门信息失败，deptId={}", deptId, e);
            }
        }

        // 转换
        return contracts.stream().map(entity -> {
            EmployeeContractDetailVO vo = new EmployeeContractDetailVO();
            // 复制基础字段
            vo.setId(entity.getId());
            vo.setEmployeeId(entity.getEmployeeId());
            vo.setContractNo(entity.getContractNo());
            vo.setContractType(entity.getContractType());
            vo.setContractTypeDesc(getContractTypeDesc(entity.getContractType()));
            vo.setStartDate(entity.getStartDate());
            vo.setEndDate(entity.getEndDate());
            vo.setProbationMonth(entity.getProbationMonth());
            vo.setProbationSalaryRatio(entity.getProbationSalaryRatio());
            vo.setAttachmentFileId(entity.getAttachmentFileId());
            vo.setSigningCount(entity.getSigningCount());
            vo.setRemark(entity.getRemark());
            vo.setCreateTime(entity.getCreateTime());

            // 设置员工信息
            EmployeeEntity employee = employeeMap.get(entity.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getEmployeeName());
                vo.setEmployeeNo(employee.getEmployeeNo());
                vo.setDeptName(deptNameMap.get(employee.getDeptId()));
            }

            return vo;
        }).collect(Collectors.toList());
    }

}

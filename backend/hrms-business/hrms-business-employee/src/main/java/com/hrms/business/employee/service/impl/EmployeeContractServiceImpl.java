package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.employee.dto.ContractCreateDTO;
import com.hrms.business.employee.dto.ContractUpdateDTO;
import com.hrms.business.employee.entity.EmployeeContractEntity;
import com.hrms.business.employee.enums.ContractTypeEnum;
import com.hrms.business.employee.mapper.EmployeeContractMapper;
import com.hrms.business.employee.service.EmployeeContractService;
import com.hrms.business.employee.vo.EmployeeContractVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工合同服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeContractServiceImpl implements EmployeeContractService {

    private final EmployeeContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeContractEntity createContract(ContractCreateDTO createDTO) {
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

}

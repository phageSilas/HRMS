package com.hrms.business.employee.service;

import com.hrms.business.employee.dto.ContractCreateDTO;
import com.hrms.business.employee.dto.ContractUpdateDTO;
import com.hrms.business.employee.entity.EmployeeContractEntity;
import com.hrms.business.employee.vo.EmployeeContractVO;

import java.util.List;

/**
 * 员工合同服务接口
 */
public interface EmployeeContractService {

    /**
     * 创建合同
     *
     * @param createDTO 创建参数
     * @return 创建后的合同实体
     */
    EmployeeContractEntity createContract(ContractCreateDTO createDTO);

    /**
     * 更新合同
     *
     * @param id        合同ID
     * @param updateDTO 更新参数
     * @return 更新后的合同实体
     */
    EmployeeContractEntity updateContract(Long id, ContractUpdateDTO updateDTO);

    /**
     * 删除合同（逻辑删除）
     *
     * @param id 合同ID
     */
    void deleteContract(Long id);

    /**
     * 获取合同详情
     *
     * @param id 合同ID
     * @return 合同详情
     */
    EmployeeContractVO getContractDetail(Long id);

    /**
     * 获取员工的合同列表
     *
     * @param employeeId 员工ID
     * @return 合同列表
     */
    List<EmployeeContractVO> getContractsByEmployee(Long employeeId);

    /**
     * 获取当前生效的合同
     *
     * @param employeeId 员工ID
     * @return 当前生效的合同
     */
    EmployeeContractEntity getCurrentContract(Long employeeId);

}

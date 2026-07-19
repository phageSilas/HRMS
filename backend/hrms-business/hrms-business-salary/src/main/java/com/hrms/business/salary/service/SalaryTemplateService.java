package com.hrms.business.salary.service;

import com.hrms.business.salary.dto.EmployeeSalaryProfileRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateCreateOrUpdateRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateQueryDTO;
import com.hrms.business.salary.vo.EmployeeSalaryProfileVO;
import com.hrms.business.salary.vo.SalaryTemplatePageVO;
import com.hrms.common.web.PageResult;

/**
 * 薪资账套服务接口。
 */
public interface SalaryTemplateService {

    /**
     * 分页查询薪资账套。
     *
     * @param queryDTO 查询参数
     * @return 薪资账套分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<SalaryTemplatePageVO> pageTemplates(SalaryTemplateQueryDTO queryDTO);

    /**
     * 创建薪资账套。
     *
     * @param requestDTO 创建请求
     * @return 创建后的薪资账套
     * 本方法使用的工具类: 无
     */
    SalaryTemplatePageVO createTemplate(SalaryTemplateCreateOrUpdateRequestDTO requestDTO);

    /**
     * 更新薪资账套。
     *
     * @param id         账套ID
     * @param requestDTO 更新请求
     * @return 更新后的薪资账套
     * 本方法使用的工具类: 无
     */
    SalaryTemplatePageVO updateTemplate(Long id, SalaryTemplateCreateOrUpdateRequestDTO requestDTO);

    /**
     * 查询员工薪资档案。
     *
     * @param employeeId 员工ID
     * @return 薪资档案
     * 本方法使用的工具类: 无
     */
    EmployeeSalaryProfileVO getEmployeeProfile(Long employeeId);

    /**
     * 设置员工薪资档案。
     *
     * @param employeeId 员工ID
     * @param requestDTO 设置请求
     * @return 设置后的薪资档案
     * 本方法使用的工具类: 无
     */
    EmployeeSalaryProfileVO setEmployeeProfile(Long employeeId, EmployeeSalaryProfileRequestDTO requestDTO);
}

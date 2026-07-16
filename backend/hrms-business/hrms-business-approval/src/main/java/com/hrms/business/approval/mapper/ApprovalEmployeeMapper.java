package com.hrms.business.approval.mapper;

import com.hrms.business.approval.dto.EmployeeBriefDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 员工信息查询 Mapper（轻量跨模块查询，不依赖 hrms-business-employee 模块）
 */
@Mapper
public interface ApprovalEmployeeMapper {

    /**
     * 根据用户ID查询员工简要信息
     */
    @Select("SELECT id, employee_name, user_id, leader_id, dept_id FROM hr_employee WHERE user_id = #{userId} AND is_deleted = 0 LIMIT 1")
    EmployeeBriefDTO findByUserId(Long userId);

    /**
     * 根据员工ID查询员工简要信息
     */
    @Select("SELECT id, employee_name, user_id, leader_id, dept_id FROM hr_employee WHERE id = #{employeeId} AND is_deleted = 0 LIMIT 1")
    EmployeeBriefDTO findById(Long employeeId);
}

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
     * 根据用户ID查询员工简要信息（含职位名称）
     */
    @Select("SELECT e.id, e.employee_name, e.user_id, e.leader_id, e.dept_id, sp.post_name " +
            "FROM hr_employee e " +
            "LEFT JOIN sys_post sp ON e.post_id = sp.id " +
            "WHERE e.user_id = #{userId} AND e.is_deleted = 0 LIMIT 1")
    EmployeeBriefDTO findByUserId(Long userId);

    /**
     * 根据员工ID查询员工简要信息（含职位名称）
     */
    @Select("SELECT e.id, e.employee_name, e.user_id, e.leader_id, e.dept_id, sp.post_name " +
            "FROM hr_employee e " +
            "LEFT JOIN sys_post sp ON e.post_id = sp.id " +
            "WHERE e.id = #{employeeId} AND e.is_deleted = 0 LIMIT 1")
    EmployeeBriefDTO findById(Long employeeId);
}

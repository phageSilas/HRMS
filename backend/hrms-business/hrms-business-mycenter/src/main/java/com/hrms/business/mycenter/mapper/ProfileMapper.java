package com.hrms.business.mycenter.mapper;

import com.hrms.business.mycenter.dto.ProfileVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 个人档案 Mapper
 * 使用自定义 SQL 直接查询 hr_employee 全字段，避免依赖 EmployeeEntity 缺失的列
 */
public interface ProfileMapper {

    /**
     * 根据用户ID查询个人档案（通过 hr_employee.user_id 关联）
     */
    @Select({
            "<script>",
            "SELECT",
            "  e.id AS employeeId,",
            "  e.employee_no AS employeeNo,",
            "  e.employee_name AS employeeName,",
            "  e.gender,",
            "  e.phone,",
            "  e.email,",
            "  e.id_card_no AS idCard,",
            "  e.birthday,",
            "  e.dept_id AS deptId,",
            "  d.dept_name AS deptName,",
            "  p.post_name AS postName,",
            "  e.job_level AS jobLevel,",
            "  e.leader_id AS leaderId,",
            "  e.hire_date AS hireDate,",
            "  e.emergency_contact AS emergencyContact,",
            "  e.emergency_phone AS emergencyPhone,",
            "  e.current_address AS currentAddress",
            "FROM hr_employee e",
            "LEFT JOIN sys_dept d ON e.dept_id = d.id",
            "LEFT JOIN sys_post p ON e.post_id = p.id",
            "WHERE e.user_id = #{userId}",
            "</script>"
    })
    ProfileVO selectProfileByUserId(@Param("userId") Long userId);

    /**
     * 根据员工ID查询个人档案（通过 hr_employee.id 关联 sys_user.employee_id）
     */
    @Select({
            "<script>",
            "SELECT",
            "  e.id AS employeeId,",
            "  e.employee_no AS employeeNo,",
            "  e.employee_name AS employeeName,",
            "  e.gender,",
            "  e.phone,",
            "  e.email,",
            "  e.id_card_no AS idCard,",
            "  e.birthday,",
            "  e.dept_id AS deptId,",
            "  d.dept_name AS deptName,",
            "  p.post_name AS postName,",
            "  e.job_level AS jobLevel,",
            "  e.leader_id AS leaderId,",
            "  e.hire_date AS hireDate,",
            "  e.emergency_contact AS emergencyContact,",
            "  e.emergency_phone AS emergencyPhone,",
            "  e.current_address AS currentAddress",
            "FROM hr_employee e",
            "LEFT JOIN sys_dept d ON e.dept_id = d.id",
            "LEFT JOIN sys_post p ON e.post_id = p.id",
            "WHERE e.id = #{employeeId}",
            "</script>"
    })
    ProfileVO selectProfileByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 更新员工可编辑字段
     */
    @Update({
            "<script>",
            "UPDATE hr_employee",
            "<set>",
            "  <if test='email != null'>email = #{email},</if>",
            "  <if test='phone != null'>phone = #{phone},</if>",
            "  <if test='currentAddress != null'>current_address = #{currentAddress},</if>",
            "  <if test='emergencyContact != null'>emergency_contact = #{emergencyContact},</if>",
            "  <if test='emergencyPhone != null'>emergency_phone = #{emergencyPhone},</if>",
            "</set>",
            "WHERE id = #{employeeId}",
            "</script>"
    })
    int updateProfile(@Param("employeeId") Long employeeId,
                      @Param("email") String email,
                      @Param("phone") String phone,
                      @Param("currentAddress") String currentAddress,
                      @Param("emergencyContact") String emergencyContact,
                      @Param("emergencyPhone") String emergencyPhone);
}

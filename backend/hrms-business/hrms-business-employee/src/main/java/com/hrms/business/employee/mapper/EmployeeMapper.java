package com.hrms.business.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.employee.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 员工 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<EmployeeEntity> {

    /**
     * 查询员工是否存在合同记录
     *
     * @param employeeId 员工ID
     * @return 合同记录数
     */
    @Select("SELECT COUNT(*) FROM hr_employee_contract WHERE employee_id = #{employeeId} AND is_deleted = 0")
    Long countContractsByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 查询员工是否存在考勤记录
     *
     * @param employeeId 员工ID
     * @return 考勤记录数
     */
    @Select("SELECT COUNT(*) FROM hr_attendance_record WHERE employee_id = #{employeeId}")
    Long countAttendanceByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 查询员工是否存在薪资记录
     *
     * @param employeeId 员工ID
     * @return 薪资记录数
     */
    @Select("SELECT COUNT(*) FROM hr_salary_batch_item WHERE employee_id = #{employeeId}")
    Long countSalaryByEmployeeId(@Param("employeeId") Long employeeId);

}

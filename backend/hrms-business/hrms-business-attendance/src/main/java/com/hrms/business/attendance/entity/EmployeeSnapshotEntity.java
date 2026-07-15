package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工快照只读实体，对应 hr_employee。
 */
@Data
@TableName("hr_employee")
public class EmployeeSnapshotEntity {

    /**
     * 员工ID。
     */
    @TableId
    private Long id;

    /**
     * 员工工号。
     */
    private String employeeNo;

    /**
     * 关联系统用户ID。
     */
    private Long userId;

    /**
     * 部门ID。
     */
    private Long deptId;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 在职状态。
     */
    private Integer employmentStatus;

    /**
     * 入职日期。
     */
    private LocalDate hireDate;
}

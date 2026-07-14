package com.hrms.business.personnel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工快照实体，仅用于入转调离模块只读查询员工基础信息。
 */
@Data
@TableName("hr_employee")
public class EmployeeSnapshotEntity extends BaseEntity {

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 直属上级员工ID
     */
    private Long leaderId;

    /**
     * 职级
     */
    private String jobLevel;

    /**
     * 在职状态：1-试用期，2-正式，3-待离职，4-已离职
     */
    private Integer employmentStatus;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 试用期（月）
     */
    private Integer probationMonth;

    /**
     * 基本工资
     */
    private BigDecimal baseSalary;

}

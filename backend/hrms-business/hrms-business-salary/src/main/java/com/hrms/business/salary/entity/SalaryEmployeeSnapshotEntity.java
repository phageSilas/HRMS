package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工快照只读实体，对齐 hr_employee 表，薪资模块仅查询不写入。
 */
@Data
@TableName("hr_employee")
public class SalaryEmployeeSnapshotEntity {

    @TableId
    private Long id;

    private String employeeNo;

    private Long userId;

    private Long deptId;

    private Long postId;

    private String employeeName;

    private Integer employmentStatus;

    private LocalDate hireDate;

    private BigDecimal probationSalaryRatio;

    private Long salaryTemplateId;

    private BigDecimal baseSalary;

    private String bankAccount;

    private String bankName;

    private Integer isDeleted;
}

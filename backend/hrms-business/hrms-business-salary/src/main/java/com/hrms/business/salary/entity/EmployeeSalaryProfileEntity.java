package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工薪资档案实体，对齐 hr_employee_salary_profile 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_salary_profile")
public class EmployeeSalaryProfileEntity extends BaseEntity {

    private Long employeeId;

    private Long templateId;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBase;

    private BigDecimal socialInsuranceBase;

    private BigDecimal housingFundBase;

    private String bankName;

    private String bankAccount;

    private LocalDate effectiveDate;

    private String remark;
}

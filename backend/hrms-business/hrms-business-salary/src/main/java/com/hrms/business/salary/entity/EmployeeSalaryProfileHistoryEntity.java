package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 员工薪资档案变更历史实体，对齐 hr_employee_salary_profile_history 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_salary_profile_history")
public class EmployeeSalaryProfileHistoryEntity extends BaseEntity {

    private Long employeeId;

    private Long templateIdBefore;

    private Long templateIdAfter;

    private BigDecimal baseSalaryBefore;

    private BigDecimal baseSalaryAfter;

    private BigDecimal allowanceBefore;

    private BigDecimal allowanceAfter;

    private BigDecimal performanceBaseBefore;

    private BigDecimal performanceBaseAfter;

    private BigDecimal socialInsuranceBaseBefore;

    private BigDecimal socialInsuranceBaseAfter;

    private BigDecimal housingFundBaseBefore;

    private BigDecimal housingFundBaseAfter;

    private BigDecimal probationSalaryRatioBefore;

    private BigDecimal probationSalaryRatioAfter;

    private String changeReason;
}

package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工薪资档案变更历史返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryProfileHistoryVO {

    private Long id;

    private Long employeeId;

    private Long templateIdBefore;

    private String templateNameBefore;

    private Long templateIdAfter;

    private String templateNameAfter;

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

    private LocalDateTime createTime;

    private Long createBy;
}

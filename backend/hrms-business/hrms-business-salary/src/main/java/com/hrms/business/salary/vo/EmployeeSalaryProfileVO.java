package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工薪资档案返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryProfileVO {

    private Long id;

    private Long employeeId;

    private String employeeNo;

    private String employeeName;

    private Long templateId;

    private String templateName;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBase;

    private BigDecimal socialInsuranceBase;

    private BigDecimal pensionInsuranceBase;

    private BigDecimal pensionInsuranceRate;

    private BigDecimal medicalInsuranceBase;

    private BigDecimal medicalInsuranceRate;

    private BigDecimal unemploymentInsuranceBase;

    private BigDecimal unemploymentInsuranceRate;

    private BigDecimal housingFundBase;

    private String bankName;

    private String bankAccountMasked;

    private LocalDate effectiveDate;

    private String remark;
}

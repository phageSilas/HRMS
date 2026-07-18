package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 薪资批次明细返回视图。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchItemVO {

    private Long id;

    private Long batchId;

    private Long employeeId;

    private String employeeNo;

    private String employeeName;

    private String deptName;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBonus;

    private BigDecimal overtimePay;

    private BigDecimal lateDeduction;

    private BigDecimal leaveDeduction;

    private BigDecimal socialInsurance;

    private BigDecimal pensionInsurance;

    private BigDecimal medicalInsurance;

    private BigDecimal unemploymentInsurance;

    private BigDecimal housingFund;

    private BigDecimal incomeTax;

    private BigDecimal grossSalary;

    private BigDecimal deductionTotal;

    private BigDecimal netSalary;

    private String warningLevel;

    private String warningReason;
}

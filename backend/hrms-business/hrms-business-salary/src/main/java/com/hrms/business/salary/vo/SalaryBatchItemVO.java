package com.hrms.business.salary.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资批次明细返回视图。
 */
@Data
public class SalaryBatchItemVO {

    private Long id;

    private Long batchId;

    private Long employeeId;

    private String employeeNo;

    private String employeeName;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBonus;

    private BigDecimal overtimePay;

    private BigDecimal lateDeduction;

    private BigDecimal leaveDeduction;

    private BigDecimal socialInsurance;

    private BigDecimal housingFund;

    private BigDecimal incomeTax;

    private BigDecimal grossSalary;

    private BigDecimal deductionTotal;

    private BigDecimal netSalary;

    private String warningLevel;

    private String warningReason;
}

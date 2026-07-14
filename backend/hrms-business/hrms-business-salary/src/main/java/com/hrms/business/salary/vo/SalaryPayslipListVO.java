package com.hrms.business.salary.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工资条列表返回视图。
 */
@Data
public class SalaryPayslipListVO {

    private Long id;

    private String salaryMonth;

    private BigDecimal grossSalary;

    private BigDecimal deductionTotal;

    private BigDecimal netSalary;

    private String batchStatus;

    private Boolean verified;
}

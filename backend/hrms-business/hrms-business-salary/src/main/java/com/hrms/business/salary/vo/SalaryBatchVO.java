package com.hrms.business.salary.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资批次返回视图。
 */
@Data
public class SalaryBatchVO {

    private Long id;

    private String batchNo;

    private String salaryMonth;

    private String scopeType;

    private String scopeValue;

    private String batchStatus;

    private Long approvalInstanceId;

    private Integer totalCount;

    private BigDecimal totalGrossSalary;

    private BigDecimal totalNetSalary;

    private Integer yellowWarningCount;

    private Integer redWarningCount;

    private Integer blockCount;
}

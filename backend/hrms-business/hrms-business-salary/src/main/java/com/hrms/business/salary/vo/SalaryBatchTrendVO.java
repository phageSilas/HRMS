package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理端薪资批次跨月份趋势返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchTrendVO {

    /**
     * 薪资月份，格式 yyyy-MM。
     */
    private String month;

    /**
     * 应发工资总额。
     */
    private BigDecimal grossSalary;

    /**
     * 实发工资总额。
     */
    private BigDecimal netSalary;

    /**
     * 核算员工数。
     */
    private Integer employeeCount;
}

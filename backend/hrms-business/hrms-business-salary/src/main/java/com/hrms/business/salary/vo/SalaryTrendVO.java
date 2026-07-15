package com.hrms.business.salary.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资趋势返回视图。
 */
@Data
public class SalaryTrendVO {

    private String month;

    private BigDecimal netSalary;
}

package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资趋势返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTrendVO {

    private String month;

    private BigDecimal netSalary;
}

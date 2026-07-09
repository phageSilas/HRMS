package com.hrms.business.salary.vo;

import java.math.BigDecimal;

/**
 * 月度薪资返回对象。
 *
 * @param id 月度薪资ID
 * @param employeeId 员工ID
 * @param salaryMonth 薪资月份
 * @param payableSalary 应发工资
 * @param actualSalary 实发工资
 * @param salaryStatus 薪资状态
 */
public record SalaryMonthlyVO(
    Long id,
    Long employeeId,
    String salaryMonth,
    BigDecimal payableSalary,
    BigDecimal actualSalary,
    String salaryStatus
) {
}

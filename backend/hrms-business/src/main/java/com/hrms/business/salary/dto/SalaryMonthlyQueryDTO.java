package com.hrms.business.salary.dto;

import com.hrms.common.model.BasePageQuery;

/**
 * 月度薪资分页查询参数。
 */
public class SalaryMonthlyQueryDTO extends BasePageQuery {

    private Long employeeId;
    private String salaryMonth;

    /**
     * 获取员工ID。
     *
     * @return 员工ID
     */
    public Long getEmployeeId() {
        return employeeId;
    }

    /**
     * 设置员工ID。
     *
     * @param employeeId 员工ID
     */
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * 获取薪资月份。
     *
     * @return 薪资月份
     */
    public String getSalaryMonth() {
        return salaryMonth;
    }

    /**
     * 设置薪资月份。
     *
     * @param salaryMonth 薪资月份
     */
    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth;
    }
}

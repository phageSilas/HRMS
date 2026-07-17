package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理端工资条分页返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryManagePayslipPageVO {

    private Long id;

    private Long batchId;

    private Long employeeId;

    private String employeeName;

    private String employeeNo;

    private Long deptId;

    private String deptName;

    private String salaryMonth;

    private BigDecimal grossSalary;

    private BigDecimal deductionTotal;

    private BigDecimal netSalary;

    private String batchStatus;

    private String publishStatus;

    private String viewStatus;

    private Boolean verified;
}

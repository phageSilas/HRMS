package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工薪资档案详情返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryProfileDetailVO {

    private Long employeeId;

    private String employeeNo;

    private String employeeName;

    private Long deptId;

    private String deptName;

    private Long postId;

    private String postName;

    private Integer employmentStatus;

    private String employmentStatusDesc;

    private Long templateId;

    private String templateName;

    private Boolean assignedTemplate;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBase;

    private BigDecimal socialInsuranceBase;

    private BigDecimal housingFundBase;

    private BigDecimal probationSalaryRatio;

    private LocalDate effectiveDate;

    private String remark;

    private List<EmployeeSalaryProfileHistoryVO> history;
}

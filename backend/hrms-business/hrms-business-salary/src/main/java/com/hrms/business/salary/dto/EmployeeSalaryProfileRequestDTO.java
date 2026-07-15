package com.hrms.business.salary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工薪资档案设置请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryProfileRequestDTO {

    private Long templateId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBase;

    private BigDecimal socialInsuranceBase;

    private BigDecimal housingFundBase;

    private String bankName;

    private String bankAccount;

    private LocalDate effectiveDate;

    private String remark;
}

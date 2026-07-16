package com.hrms.business.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资账套项目请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateItemRequestDTO {

    @NotBlank
    private String itemCode;

    @NotBlank
    private String itemName;

    @NotBlank
    private String category;

    private String calcRule;

    private BigDecimal defaultValue;

    private Integer sortNo;
}

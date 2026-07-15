package com.hrms.business.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资账套项目请求参数。
 */
@Data
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

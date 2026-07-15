package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资账套项目返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateItemVO {

    private Long id;

    private String itemCode;

    private String itemName;

    private String category;

    private String calcRule;

    private BigDecimal defaultValue;

    private Integer sortNo;
}

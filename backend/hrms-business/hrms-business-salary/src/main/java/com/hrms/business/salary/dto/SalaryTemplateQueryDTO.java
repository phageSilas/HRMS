package com.hrms.business.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪资账套分页查询参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateQueryDTO {

    private String templateName;

    private String scope;

    private Long employeeId;

    private Integer status;

    @Min(1)
    @Builder.Default
    private Integer pageNum = 1;

    @Min(1)
    @Max(200)
    @Builder.Default
    private Integer pageSize = 10;
}

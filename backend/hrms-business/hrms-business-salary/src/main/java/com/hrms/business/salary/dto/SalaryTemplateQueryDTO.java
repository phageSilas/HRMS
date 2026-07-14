package com.hrms.business.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 薪资账套分页查询参数。
 */
@Data
public class SalaryTemplateQueryDTO {

    private String templateName;

    private Integer status;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(200)
    private Integer pageSize = 10;
}

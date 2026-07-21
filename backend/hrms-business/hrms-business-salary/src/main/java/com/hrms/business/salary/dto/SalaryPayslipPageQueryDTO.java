package com.hrms.business.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 员工端工资条分页查询参数。
 */
@Data
public class SalaryPayslipPageQueryDTO {

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "薪资月份格式必须为yyyy-MM")
    private String month;

    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

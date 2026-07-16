package com.hrms.business.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工号生成响应 VO
 */
@Data
@Schema(description = "工号生成响应")
public class EmployeeGenNoVO {

    @Schema(description = "生成的员工工号", example = "2026BE005")
    private String employeeNo;

}

package com.hrms.business.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工合同详情 VO（包含员工信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "员工合同详情（包含员工信息）")
public class EmployeeContractDetailVO extends EmployeeContractVO {

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "部门名称")
    private String deptName;

}

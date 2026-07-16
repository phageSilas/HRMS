package com.hrms.business.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工列表 VO
 */
@Data
@Schema(description = "员工列表项")
public class EmployeeListVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "性别：1-男 2-女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "职级")
    private String jobLevel;

    @Schema(description = "在职状态：1-试用期 2-正式 3-待离职 4-已离职")
    private Integer employmentStatus;

    @Schema(description = "在职状态描述")
    private String employmentStatusDesc;

    @Schema(description = "入职日期")
    private LocalDate hireDate;

    @Schema(description = "直接汇报人姓名")
    private String leaderName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

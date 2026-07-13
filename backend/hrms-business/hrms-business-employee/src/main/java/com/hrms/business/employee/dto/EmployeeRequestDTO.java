package com.hrms.business.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 员工请求 DTO
 */
@Data
@Schema(description = "员工请求")
public class EmployeeRequestDTO {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：1-男，2-女，0-未知")
    private Integer gender;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "身份证号")
    private String idCardNo;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "职位ID")
    private Long postId;

    @Schema(description = "入职日期")
    private String hireDate;

    @Schema(description = "状态：1-试用期，2-正式，3-离职")
    private Integer status;

    @Schema(description = "工作地点")
    private String workLocation;

    @Schema(description = "最高学历")
    private String education;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "毕业院校")
    private String school;

    @Schema(description = "合同开始日期")
    private String contractStartDate;

    @Schema(description = "合同结束日期")
    private String contractEndDate;

}

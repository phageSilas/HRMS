package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 个人档案 VO
 */
@Data
@Schema(description = "个人档案")
public class ProfileVO {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "姓名")
    private String employeeName;

    @Schema(description = "性别：1-男 2-女 0-未知")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "身份证号（脱敏）")
    private String idCard;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位")
    private String postName;

    @Schema(description = "职级")
    private String jobLevel;

    @Schema(description = "直接上级员工ID")
    private Long leaderId;

    @Schema(description = "入职日期")
    private String hireDate;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @Schema(description = "现居地址")
    private String currentAddress;

    @Schema(description = "字段权限")
    private FieldPermissions fieldPermissions;

    @Data
    @Schema(description = "字段权限")
    public static class FieldPermissions {

        @Schema(description = "可直接编辑的字段")
        private List<String> editableFields;

        @Schema(description = "需走流程的字段")
        private List<String> flowRequiredFields;

        @Schema(description = "锁定字段（不可编辑、不可查看完整值）")
        private List<String> lockedFields;
    }
}

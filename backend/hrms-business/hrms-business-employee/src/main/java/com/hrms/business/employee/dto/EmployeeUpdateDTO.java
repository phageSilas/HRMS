package com.hrms.business.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新员工 DTO
 */
@Data
@Schema(description = "更新员工请求参数")
public class EmployeeUpdateDTO {

    @Size(max = 64, message = "员工姓名长度不能超过64")
    @Schema(description = "员工姓名", example = "赵六")
    private String employeeName;

    @Schema(description = "性别：1-男 2-女", example = "1")
    private Integer gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13900001111")
    private String phone;

    @Schema(description = "邮箱", example = "zhaoliu@hrms.com")
    private String email;

    @Schema(description = "所属部门ID", example = "10")
    private Long deptId;

    @Schema(description = "职位ID", example = "102")
    private Long postId;

    @Schema(description = "职级", example = "P4")
    private String jobLevel;

    @Schema(description = "直接汇报人员工ID", example = "5")
    private Long leaderId;

    @Schema(description = "工作地点", example = "杭州")
    private String workLocation;

    @Schema(description = "入职类型：1-全职 2-兼职 3-实习", example = "1")
    private Integer hireType;

    @Schema(description = "在职状态：1-试用期 2-正式 3-待离职 4-已离职", example = "2")
    private Integer employmentStatus;

    @Schema(description = "入职日期", example = "2026-07-15")
    private LocalDate hireDate;

    @Schema(description = "试用期（月）", example = "6")
    private Integer probationMonth;

    @Schema(description = "试用期薪资比例（%）", example = "80.00")
    private BigDecimal probationSalaryRatio;

    @Schema(description = "合同类型：1-固定期限 2-无固定期限 3-劳务合同", example = "1")
    private Integer contractType;

    @Schema(description = "合同到期日", example = "2029-07-14")
    private LocalDate contractExpireDate;

    @Schema(description = "薪资账套ID", example = "2")
    private Long salaryTemplateId;

    @Schema(description = "基本工资", example = "12000.00")
    private BigDecimal baseSalary;

    @Schema(description = "身份证号")
    private String idCardNo;

    @Schema(description = "生日", example = "1995-06-15")
    private LocalDate birthday;

    @Schema(description = "户籍地址")
    private String domicileAddress;

    @Schema(description = "现居住地址")
    private String currentAddress;

    @Schema(description = "银行账号")
    private String bankAccount;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @Schema(description = "备注")
    private String remark;

}

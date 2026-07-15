package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入职申请创建或更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职申请创建或更新请求")
public class EntryApplicationCreateOrUpdateRequestDTO {

    /**
     * 候选人姓名
     */
    @NotBlank(message = "候选人姓名不能为空")
    @Schema(description = "候选人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String candidateName;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = 0, message = "性别取值不正确")
    @Max(value = 2, message = "性别取值不正确")
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;

    /**
     * 拟入职部门ID
     */
    @NotNull(message = "拟入职部门不能为空")
    @JsonAlias("departmentId")
    @Schema(description = "拟入职部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;

    /**
     * 拟入职岗位ID
     */
    @NotNull(message = "拟入职岗位不能为空")
    @JsonAlias("positionId")
    @Schema(description = "拟入职岗位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /**
     * 录用类型：1-全职，2-兼职，3-实习
     */
    @NotNull(message = "录用类型不能为空")
    @Min(value = 1, message = "录用类型取值不正确")
    @Max(value = 3, message = "录用类型取值不正确")
    @Schema(description = "录用类型：1-全职，2-兼职，3-实习", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hireType;

    /**
     * 试用期（月）
     */
    @NotNull(message = "试用期不能为空")
    @Min(value = 0, message = "试用期不能小于0")
    @Max(value = 12, message = "试用期不能超过12个月")
    @JsonAlias("probationMonths")
    @Schema(description = "试用期（月）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer probationMonth;

    /**
     * 试用期薪资比例
     */
    @DecimalMin(value = "0.00", message = "试用期薪资比例不能小于0")
    @Schema(description = "试用期薪资比例，默认80.00")
    private BigDecimal probationSalaryRatio;

    /**
     * 预计入职日期
     */
    @NotNull(message = "预计入职日期不能为空")
    @JsonAlias({"expectedEntryDate", "hireDate"})
    @Schema(description = "预计入职日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expectedHireDate;

    /**
     * 直接汇报人ID
     */
    @JsonAlias("reporterId")
    @Schema(description = "直接汇报人ID")
    private Long leaderId;

    /**
     * 备注，当前入职申请表暂未落库
     */
    @Schema(description = "备注，当前入职申请表暂未落库")
    private String remark;

}

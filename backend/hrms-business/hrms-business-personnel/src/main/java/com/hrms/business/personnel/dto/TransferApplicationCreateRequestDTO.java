package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 调岗申请创建请求 DTO
 */
@Data
@Schema(description = "调岗申请创建请求")
public class TransferApplicationCreateRequestDTO {

    /**
     * 调岗员工ID
     */
    @NotNull(message = "调岗员工不能为空")
    @Schema(description = "调岗员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    /**
     * 新部门ID
     */
    @NotNull(message = "新部门不能为空")
    @JsonAlias("newDepartmentId")
    @Schema(description = "新部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toDeptId;

    /**
     * 新岗位ID
     */
    @NotNull(message = "新岗位不能为空")
    @JsonAlias("newPositionId")
    @Schema(description = "新岗位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toPostId;

    /**
     * 新职级
     */
    @JsonAlias("newGrade")
    @Schema(description = "新职级")
    private String toJobLevel;

    /**
     * 新汇报人ID
     */
    @JsonAlias("newReporterId")
    @Schema(description = "新汇报人ID")
    private Long toLeaderId;

    /**
     * 生效日期
     */
    @NotNull(message = "生效日期不能为空")
    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate effectiveDate;

    /**
     * 薪资调整金额
     */
    @Schema(description = "薪资调整金额")
    private BigDecimal salaryAdjustment;

    /**
     * 调岗原因
     */
    @Schema(description = "调岗原因")
    private String reason;

}

package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 离职申请创建请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "离职申请创建请求")
public class LeaveApplicationCreateRequestDTO {

    /**
     * 离职员工ID
     */
    @NotNull(message = "离职员工不能为空")
    @Schema(description = "离职员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    /**
     * 离职类型
     */
    @NotBlank(message = "离职类型不能为空")
    @Pattern(regexp = "^(resign|terminate|mutual|contract_end)$", message = "离职类型取值不正确")
    @Schema(description = "离职类型：resign / terminate / mutual / contract_end", requiredMode = Schema.RequiredMode.REQUIRED)
    private String leaveType;

    /**
     * 离职原因
     */
    @NotBlank(message = "离职原因不能为空")
    @JsonAlias("reason")
    @Schema(description = "离职原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String leaveReason;

    /**
     * 最后工作日
     */
    @NotNull(message = "最后工作日不能为空")
    @Schema(description = "最后工作日", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate lastWorkDate;

    /**
     * 工作交接人ID
     */
    @NotNull(message = "工作交接人不能为空")
    @Schema(description = "工作交接人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long handoverEmployeeId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}

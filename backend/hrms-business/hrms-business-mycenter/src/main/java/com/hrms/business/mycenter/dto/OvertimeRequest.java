package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班申请请求 DTO
 */
@Data
@Schema(description = "加班申请请求")
public class OvertimeRequest {

    @NotNull(message = "加班日期不能为空")
    @Schema(description = "加班日期", example = "2026-07-20T09:00:00")
    private LocalDateTime overtimeDate;

    @NotNull(message = "加班时长不能为空")
    @DecimalMin(value = "0.5", message = "加班时长至少0.5小时")
    @DecimalMax(value = "24", message = "单次加班不超过24小时")
    @Schema(description = "加班时长（小时）", example = "2.5")
    private BigDecimal duration;

    @NotBlank(message = "加班事由不能为空")
    @Schema(description = "加班事由", example = "项目上线紧急支持")
    private String reason;
}

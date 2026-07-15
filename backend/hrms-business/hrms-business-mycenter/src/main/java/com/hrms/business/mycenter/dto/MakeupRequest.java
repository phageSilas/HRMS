package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 补卡申请请求 DTO
 */
@Data
@Schema(description = "补卡申请请求")
public class MakeupRequest {

    @NotNull(message = "补卡日期不能为空")
    @Schema(description = "补卡日期")
    private LocalDate correctionDate;

    @NotBlank(message = "补卡类型不能为空")
    @Schema(description = "补卡类型：CLOCK_IN-上班补卡 CLOCK_OUT-下班补卡")
    private String correctionType;

    @NotBlank(message = "补卡原因不能为空")
    @Schema(description = "补卡原因")
    private String correctionReason;
}

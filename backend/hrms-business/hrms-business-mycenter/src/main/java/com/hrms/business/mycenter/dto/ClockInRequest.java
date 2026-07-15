package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打卡请求 DTO
 */
@Data
@Schema(description = "打卡请求")
public class ClockInRequest {

    @NotNull(message = "打卡类型不能为空")
    @Schema(description = "打卡类型：1-上班打卡 2-下班打卡")
    private Integer type;
}

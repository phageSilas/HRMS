package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 委托创建请求体
 */
@Data
@Schema(description = "委托创建请求")
public class DelegationCreateRequest {

    @NotNull(message = "被委托人不能为空")
    @Schema(description = "被委托人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long delegateeId;

    @NotBlank(message = "生效时间不能为空")
    @Schema(description = "生效时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;

    @Schema(description = "委托原因")
    private String reason;
}

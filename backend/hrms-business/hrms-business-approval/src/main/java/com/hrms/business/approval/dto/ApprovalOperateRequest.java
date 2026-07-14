package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审批操作请求体
 */
@Data
@Schema(description = "审批操作请求")
public class ApprovalOperateRequest {

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：approve（通过）/ reject（驳回）/ transfer（转交）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "转交目标用户ID（action=transfer 时必填）")
    private Long targetUserId;
}

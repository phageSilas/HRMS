package com.hrms.business.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审批任务发起请求参数。
 *
 * @param bizType 业务类型
 * @param bizId 业务主键ID
 * @param applicantId 申请人ID
 */
public record ApprovalStartRequestDTO(
    @NotBlank(message = "不能为空")
    String bizType,
    @NotNull(message = "不能为空")
    Long bizId,
    @NotNull(message = "不能为空")
    Long applicantId
) {
}

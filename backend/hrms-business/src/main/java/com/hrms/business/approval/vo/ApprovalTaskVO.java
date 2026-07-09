package com.hrms.business.approval.vo;

/**
 * 审批任务返回对象。
 *
 * @param taskId 审批任务ID
 * @param bizType 业务类型
 * @param bizId 业务主键ID
 * @param approvalStatus 审批状态
 */
public record ApprovalTaskVO(
    Long taskId,
    String bizType,
    Long bizId,
    String approvalStatus
) {
}

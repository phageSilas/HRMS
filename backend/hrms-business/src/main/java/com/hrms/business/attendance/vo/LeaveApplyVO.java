package com.hrms.business.attendance.vo;

/**
 * 请假申请返回对象。
 *
 * @param applyId 申请ID
 * @param approvalStatus 审批状态
 */
public record LeaveApplyVO(Long applyId, String approvalStatus) {
}

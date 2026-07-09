/**
 * 审批状态枚举
 * 对应后端 ApprovalStatusEnum
 */

export const ApprovalStatus = {
  DRAFT: 0,      // 草稿
  PENDING: 1,    // 审批中
  APPROVED: 2,   // 已通过
  REJECTED: 3,   // 已驳回
  WITHDRAWN: 4,  // 已撤回
} as const;

export type ApprovalStatusType = typeof ApprovalStatus[keyof typeof ApprovalStatus];

/**
 * 审批状态标签映射
 */
export const ApprovalStatusLabel: Record<number, string> = {
  [ApprovalStatus.DRAFT]: '草稿',
  [ApprovalStatus.PENDING]: '审批中',
  [ApprovalStatus.APPROVED]: '已通过',
  [ApprovalStatus.REJECTED]: '已驳回',
  [ApprovalStatus.WITHDRAWN]: '已撤回',
};
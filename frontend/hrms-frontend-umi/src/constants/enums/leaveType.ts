/**
 * 请假类型枚举
 * 对应后端 LeaveTypeEnum
 */

export const LeaveType = {
  ANNUAL: 1,       // 年假
  SICK: 2,         // 病假
  PERSONAL: 3,     // 事假
  MATERNITY: 4,    // 产假
  PATERNITY: 5,    // 陪产假
  MARRIAGE: 6,     // 婚假
  FUNERAL: 7,      // 丧假
} as const;

export type LeaveTypeType = typeof LeaveType[keyof typeof LeaveType];

/**
 * 请假类型标签映射
 */
export const LeaveTypeLabel: Record<number, string> = {
  [LeaveType.ANNUAL]: '年假',
  [LeaveType.SICK]: '病假',
  [LeaveType.PERSONAL]: '事假',
  [LeaveType.MATERNITY]: '产假',
  [LeaveType.PATERNITY]: '陪产假',
  [LeaveType.MARRIAGE]: '婚假',
  [LeaveType.FUNERAL]: '丧假',
};
/**
 * 员工状态枚举
 * 对应后端 EmployeeStatusEnum
 */

export const EmployeeStatus = {
  PROBATION: 1,      // 试用期
  FORMAL: 2,         // 正式
  PENDING_LEAVE: 3,  // 待离职
  RESIGNED: 4,       // 已离职
} as const;

export type EmployeeStatusType = typeof EmployeeStatus[keyof typeof EmployeeStatus];

/**
 * 员工状态标签映射
 */
export const EmployeeStatusLabel: Record<number, string> = {
  [EmployeeStatus.PROBATION]: '试用期',
  [EmployeeStatus.FORMAL]: '正式',
  [EmployeeStatus.PENDING_LEAVE]: '待离职',
  [EmployeeStatus.RESIGNED]: '已离职',
};
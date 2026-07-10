/**
 * 权限标识常量
 * 与后端 sys_menu.permission 字段保持一致
 */

/**
 * 权限标识枚举
 */
export const PERMISSION = {
  // 系统管理
  SYSTEM: 'system',
  SYSTEM_USER: 'system:user',
  SYSTEM_ROLE: 'system:role',
  SYSTEM_MENU: 'system:menu',
  SYSTEM_DEPT: 'system:dept',
  SYSTEM_POST: 'system:post',
  SYSTEM_DICT: 'system:dict',

  // 员工档案
  EMPLOYEE: 'employee',
  EMPLOYEE_LIST: 'employee:list',
  EMPLOYEE_DETAIL: 'employee:detail',
  EMPLOYEE_CONTRACT: 'employee:contract',
  EMPLOYEE_ADD: 'employee:add',
  EMPLOYEE_EDIT: 'employee:edit',
  EMPLOYEE_DELETE: 'employee:delete',

  // 入转调离
  PROCESS: 'process',
  PROCESS_ENTRY: 'process:entry',
  PROCESS_REGULAR: 'process:regular',
  PROCESS_TRANSFER: 'process:transfer',
  PROCESS_LEAVE: 'process:leave',

  // 考勤管理
  ATTENDANCE: 'attendance',
  ATTENDANCE_RECORD: 'attendance:record',
  ATTENDANCE_LEAVE: 'attendance:leave',
  ATTENDANCE_SUMMARY: 'attendance:summary',

  // 薪资管理
  SALARY: 'salary',
  SALARY_ACCOUNT: 'salary:account',
  SALARY_BATCH: 'salary:batch',
  SALARY_PAYSLIP: 'salary:payslip',

  // 审批中心
  APPROVAL: 'approval',
  APPROVAL_PENDING: 'approval:pending',
  APPROVAL_DONE: 'approval:done',
  APPROVAL_DETAIL: 'approval:detail',

  // 个人中心
  MYCENTER: 'mycenter',
  MYCENTER_PROFILE: 'mycenter:profile',
  MYCENTER_PASSWORD: 'mycenter:password',
} as const;

/**
 * 角色权限映射
 * 定义每个角色拥有的权限列表
 */
export const ROLE_PERMISSIONS: Record<string, string[]> = {
  ADMIN: [
    PERMISSION.SYSTEM,
    PERMISSION.EMPLOYEE,
    PERMISSION.PROCESS,
    PERMISSION.ATTENDANCE,
    PERMISSION.SALARY,
    PERMISSION.APPROVAL,
    PERMISSION.MYCENTER,
  ],
  HR: [
    PERMISSION.EMPLOYEE,
    PERMISSION.PROCESS,
    PERMISSION.ATTENDANCE,
    PERMISSION.SALARY,
    PERMISSION.APPROVAL,
    PERMISSION.MYCENTER,
  ],
  MANAGER: [
    PERMISSION.EMPLOYEE,
    PERMISSION.PROCESS,
    PERMISSION.ATTENDANCE,
    PERMISSION.APPROVAL,
    PERMISSION.MYCENTER,
  ],
  FINANCE: [
    PERMISSION.SALARY,
    PERMISSION.APPROVAL,
    PERMISSION.MYCENTER,
  ],
  EMPLOYEE: [
    PERMISSION.MYCENTER,
  ],
};

/**
 * 检查角色是否有指定权限
 */
export function hasPermission(role: string, permission: string): boolean {
  const permissions = ROLE_PERMISSIONS[role] || [];
  return permissions.includes(permission);
}
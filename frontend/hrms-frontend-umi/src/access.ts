/**
 * 权限规则定义
 * 使用 Umi access 插件实现权限控制
 */

import type { UserInfo } from '@/types/user';

/**
 * 权限规则定义函数
 * @param initialState 全局状态，包含当前用户信息
 */
export default function access(initialState: { currentUser?: UserInfo }) {
  const { currentUser } = initialState || {};
  const permissions = currentUser?.permissions || [];

  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.includes(permission);
  };

  return {
    // 模块权限
    system: hasPermission('system'),
    employee: hasPermission('employee'),
    process: hasPermission('process'),
    attendance: hasPermission('attendance'),
    salary: hasPermission('salary'),
    approval: hasPermission('approval'),
    mycenter: true, // 个人中心所有人可见

    // 辅助方法
    hasPermission,

    // 角色判断
    isAdmin: currentUser?.roleCode === 'ADMIN',
    isHR: currentUser?.roleCode === 'HR',
    isManager: currentUser?.roleCode === 'MANAGER',
    isFinance: currentUser?.roleCode === 'FINANCE',
    isEmployee: currentUser?.roleCode === 'EMPLOYEE',

    // 组合角色判断
    canViewEmployee: ['ADMIN', 'HR', 'MANAGER'].includes(currentUser?.roleCode || ''),
    canViewSalary: ['ADMIN', 'HR', 'FINANCE'].includes(currentUser?.roleCode || ''),
    canApprove: ['ADMIN', 'HR', 'MANAGER', 'FINANCE'].includes(currentUser?.roleCode || ''),
  };
}
/**
 * 菜单配置
 * 定义系统菜单结构
 */

import type { MenuItem } from '@/types/menu';

/**
 * 菜单配置列表
 * access 字段对应权限标识，无 access 则所有人可见
 */
export const menuConfig: MenuItem[] = [
  {
    key: 'home',
    name: '首页',
    icon: 'HomeOutlined',
    path: '/home',
  },
  {
    key: 'system',
    name: '系统管理',
    icon: 'SettingOutlined',
    path: '/system',
    access: 'system',
    children: [
      { key: 'system-user', name: '用户管理', icon: '', path: '/system/user' },
      { key: 'system-role', name: '角色管理', icon: '', path: '/system/role' },
      { key: 'system-menu', name: '菜单管理', icon: '', path: '/system/menu' },
      { key: 'system-dept', name: '部门管理', icon: '', path: '/system/dept' },
      { key: 'system-post', name: '职位管理', icon: '', path: '/system/post' },
      { key: 'system-dict', name: '字典管理', icon: '', path: '/system/dict' },
    ],
  },
  {
    key: 'employee',
    name: '员工档案',
    icon: 'TeamOutlined',
    path: '/employee',
    access: 'employee',
    children: [
      { key: 'employee-list', name: '员工列表', icon: '', path: '/employee/list' },
      { key: 'employee-contract', name: '合同管理', icon: '', path: '/employee/contract' },
    ],
  },
  {
    key: 'process',
    name: '入转调离',
    icon: 'SwapOutlined',
    path: '/process',
    access: 'process',
    children: [
      { key: 'process-entry', name: '入职申请', icon: '', path: '/process/entry' },
      { key: 'process-regular', name: '转正申请', icon: '', path: '/process/regular' },
      { key: 'process-transfer', name: '调岗申请', icon: '', path: '/process/transfer' },
      { key: 'process-leave', name: '离职申请', icon: '', path: '/process/leave' },
    ],
  },
  {
    key: 'attendance',
    name: '考勤管理',
    icon: 'ClockCircleOutlined',
    path: '/attendance',
    access: 'attendance',
    children: [
      { key: 'attendance-record', name: '考勤记录', icon: '', path: '/attendance/record' },
      { key: 'attendance-leave', name: '请假申请', icon: '', path: '/attendance/leave' },
      { key: 'attendance-summary', name: '考勤统计', icon: '', path: '/attendance/summary' },
    ],
  },
  {
    key: 'salary',
    name: '薪资管理',
    icon: 'PayCircleOutlined',
    path: '/salary',
    access: 'salary',
    children: [
      { key: 'salary-account', name: '薪资账套', icon: '', path: '/salary/account' },
      { key: 'salary-batch', name: '薪资核算', icon: '', path: '/salary/batch' },
      { key: 'salary-payslip', name: '工资条', icon: '', path: '/salary/payslip' },
    ],
  },
  {
    key: 'approval',
    name: '审批中心',
    icon: 'CheckCircleOutlined',
    path: '/approval',
    access: 'approval',
    children: [
      { key: 'approval-pending', name: '待办任务', icon: '', path: '/approval/pending' },
      { key: 'approval-done', name: '已办任务', icon: '', path: '/approval/done' },
    ],
  },
  {
    key: 'profile',
    name: '个人中心',
    icon: 'UserOutlined',
    path: '/profile',
    children: [
      { key: 'profile-index', name: '我的首页', icon: '', path: '/profile/index' },
    ],
  },
];

/**
 * 根据权限过滤菜单
 */
export function filterMenusByAccess(
  menus: MenuItem[],
  hasAccess: (access: string) => boolean
): MenuItem[] {
  return menus
    .filter((menu) => {
      // 无权限要求或有权限则显示
      if (!menu.access) return true;
      return hasAccess(menu.access);
    })
    .map((menu) => {
      if (menu.children) {
        return {
          ...menu,
          children: filterMenusByAccess(menu.children, hasAccess),
        };
      }
      return menu;
    })
    .filter((menu) => {
      // 如果子菜单全部被过滤掉，则隐藏父菜单
      if (menu.children && menu.children.length === 0) return false;
      return true;
    });
}
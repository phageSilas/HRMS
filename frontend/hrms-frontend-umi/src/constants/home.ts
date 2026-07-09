/**
 * 首页统计卡片配置
 */

import type { RoleCode } from '@/types/user';

/**
 * 统计卡片配置
 */
export interface StatCardConfig {
  key: string;
  title: string;
  icon: string;
  color: string;
  apiPath: string;
  dataField: string;
  unit?: string;
  roles: RoleCode[];
}

/**
 * 统计卡片配置列表
 */
export const statCardConfig: StatCardConfig[] = [
  {
    key: 'employeeCount',
    title: '员工总数',
    icon: 'TeamOutlined',
    color: '#1890ff',
    apiPath: '/employees/count',
    dataField: 'count',
    unit: '人',
    roles: ['ADMIN', 'HR'],
  },
  {
    key: 'monthEntry',
    title: '本月入职',
    icon: 'UserAddOutlined',
    color: '#52c41a',
    apiPath: '/employees/count/month-entry',
    dataField: 'count',
    unit: '人',
    roles: ['ADMIN', 'HR', 'MANAGER'],
  },
  {
    key: 'pendingApproval',
    title: '待审批',
    icon: 'CheckCircleOutlined',
    color: '#faad14',
    apiPath: '/approval/pending-count',
    dataField: 'count',
    unit: '项',
    roles: ['ADMIN', 'HR', 'MANAGER', 'FINANCE'],
  },
  {
    key: 'monthSalary',
    title: '本月薪资',
    icon: 'PayCircleOutlined',
    color: '#722ed1',
    apiPath: '/salary/monthly-total',
    dataField: 'total',
    unit: '元',
    roles: ['ADMIN', 'HR', 'FINANCE'],
  },
  {
    key: 'monthAttendance',
    title: '本月出勤',
    icon: 'ClockCircleOutlined',
    color: '#13c2c2',
    apiPath: '/my/attendance/summary',
    dataField: 'workDays',
    unit: '天',
    roles: ['EMPLOYEE'],
  },
  {
    key: 'annualLeave',
    title: '年假余额',
    icon: 'CalendarOutlined',
    color: '#eb2f96',
    apiPath: '/my/leave-balance',
    dataField: 'annualLeave',
    unit: '天',
    roles: ['EMPLOYEE'],
  },
];

/**
 * 根据角色获取可见的统计卡片
 */
export function getStatCardsByRole(role: RoleCode): StatCardConfig[] {
  return statCardConfig.filter((card) => card.roles.includes(role));
}
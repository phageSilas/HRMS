import {
  ApartmentOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  HomeOutlined,
  PayCircleOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
  SwapOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import React, { type ReactNode } from 'react';

export interface ShellRouteMeta {
  path: string;
  title: string;
  group: string;
  icon?: ReactNode;
  fixed?: boolean;
  keywords?: string[];
  match?: RegExp;
}

export const shellRoutes: ShellRouteMeta[] = [
  {
    path: '/home',
    title: '首页',
    group: '工作台',
    icon: <HomeOutlined />,
    fixed: true,
    keywords: ['工作台', '首页'],
  },
  {
    path: '/system/user',
    title: '用户管理',
    group: '权限体系',
    icon: <SafetyCertificateOutlined />,
    keywords: ['用户', '账号', '系统'],
  },
  { path: '/system/role', title: '角色管理', group: '权限体系', keywords: ['角色', '权限'] },
  { path: '/system/menu', title: '菜单管理', group: '权限体系', keywords: ['菜单'] },
  { path: '/system/dept', title: '部门管理', group: '权限体系', keywords: ['部门'] },
  { path: '/system/post', title: '职位管理', group: '权限体系', keywords: ['职位'] },
  { path: '/system/dict', title: '字典管理', group: '权限体系', keywords: ['字典'] },
  {
    path: '/organization',
    title: '组织架构',
    group: '组织架构',
    icon: <ApartmentOutlined />,
    keywords: ['组织', '架构', '部门'],
  },
  {
    path: '/employee/list',
    title: '员工列表',
    group: '员工档案',
    icon: <TeamOutlined />,
    keywords: ['员工', '档案', '列表'],
  },
  {
    path: '/employee/create',
    title: '新增员工',
    group: '员工档案',
    keywords: ['员工', '新增', '创建'],
  },
  {
    path: '/employee/:id/edit',
    title: '编辑员工',
    group: '员工档案',
    keywords: ['员工', '编辑'],
    match: /^\/employee\/[^/]+\/edit$/,
  },
  {
    path: '/employee/contract',
    title: '合同管理',
    group: '员工档案',
    keywords: ['合同'],
  },
  {
    path: '/process/entry',
    title: '入职申请',
    group: '入转调离',
    icon: <SwapOutlined />,
    keywords: ['入职', '申请', '入转调离'],
  },
  { path: '/process/regular', title: '转正申请', group: '入转调离', keywords: ['转正'] },
  { path: '/process/transfer', title: '调岗申请', group: '入转调离', keywords: ['调岗'] },
  { path: '/process/leave', title: '离职申请', group: '入转调离', keywords: ['离职'] },
  {
    path: '/attendance/punch',
    title: '员工打卡',
    group: '考勤管理',
    icon: <ClockCircleOutlined />,
    keywords: ['考勤', '打卡', '上班打卡', '下班打卡'],
  },
  {
    path: '/attendance/record',
    title: '考勤记录',
    group: '考勤管理',
    icon: <ClockCircleOutlined />,
    keywords: ['考勤', '打卡', '记录'],
  },
  {
    path: '/attendance/groups',
    title: '考勤配置',
    group: '考勤管理',
    icon: <ClockCircleOutlined />,
    keywords: ['考勤', '配置', '考勤组', '打卡规则'],
  },
  { path: '/attendance/leaveManage', title: '请假管理', group: '考勤管理', keywords: ['请假', '请假管理', '审批'] },
  { path: '/attendance/summary', title: '考勤统计', group: '考勤管理', keywords: ['统计'] },
  {
    path: '/salary/account',
    title: '薪资账套',
    group: '薪资管理',
    icon: <PayCircleOutlined />,
    keywords: ['薪资', '账套'],
  },
  { path: '/salary/batch', title: '薪资核算', group: '薪资管理', keywords: ['薪资', '核算'] },
  { path: '/salary/payslip', title: '工资条', group: '薪资管理', keywords: ['工资条'] },
  {
    path: '/approval/workspace',
    title: '审批工作台',
    group: '审批中心',
    icon: <CheckCircleOutlined />,
    keywords: ['审批', '待办'],
  },
  { path: '/approval/delegation', title: '审批配置', group: '审批中心', keywords: ['审批', '配置'] },
  {
    path: '/profile/index',
    title: '个人中心',
    group: '个人中心',
    icon: <UserOutlined />,
    keywords: ['个人', '我的'],
  },
  { path: '/profile/archive', title: '我的档案', group: '个人中心', keywords: ['我的档案'] },
  { path: '/profile/attendance', title: '我的考勤', group: '个人中心', keywords: ['我的考勤'] },
  { path: '/profile/leave', title: '我的请假', group: '个人中心', keywords: ['我的请假'] },
  { path: '/profile/salary', title: '我的薪资', group: '个人中心', keywords: ['我的薪资'] },
  { path: '/profile/security', title: '账号安全', group: '个人中心', keywords: ['安全', '密码'] },
  {
    path: '/ai',
    title: 'AI 智能助手',
    group: 'AI 智能助手',
    icon: <RobotOutlined />,
    keywords: ['AI', '助手', '智能'],
  },
];

export function getRouteMeta(pathname: string): ShellRouteMeta {
  const normalized = pathname === '/' ? '/home' : pathname;
  const exact = shellRoutes.find((item) => item.path === normalized);
  if (exact) {
    return exact;
  }

  const matched = shellRoutes.find((item) => item.match?.test(normalized));
  if (matched) {
    return {
      ...matched,
      path: normalized,
    };
  }

  return {
    path: normalized,
    title: '业务页面',
    group: 'HRMS',
    keywords: [],
  };
}

export function getRouteTrail(pathname: string) {
  const meta = getRouteMeta(pathname);
  if (meta.path === '/home') {
    return [{ title: '首页', path: '/home' }];
  }
  return [
    { title: meta.group, path: meta.path },
    { title: meta.title, path: meta.path },
  ];
}

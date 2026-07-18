/**
 * 用户相关类型定义
 */

import type { MenuItem } from './menu';

/**
 * 用户信息
 */
export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  realName?: string;
  avatar?: string;
  deptId?: number;
  deptName?: string;
  roleCode: string;
  roleName?: string;
  permissions: string[];
  /** 用户菜单树 */
  menus?: MenuItem[];
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应结果
 */
export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  roleCode: string;
  permissions: string[];
}

/**
 * 角色枚举
 */
export enum RoleCode {
  ADMIN = 'ADMIN',
  HR = 'HR',
  MANAGER = 'MANAGER',
  FINANCE = 'FINANCE',
  EMPLOYEE = 'EMPLOYEE',
}

/**
 * 角色配置
 */
export interface RoleConfig {
  code: string;
  name: string;
  description: string;
}

/**
 * 角色列表（用于角色选择下拉框）
 */
export const ROLE_LIST: RoleConfig[] = [
  { code: RoleCode.ADMIN, name: '系统管理员', description: '拥有所有权限' },
  { code: RoleCode.HR, name: 'HR 专员', description: '管理员工档案、考勤、薪资' },
  { code: RoleCode.MANAGER, name: '部门主管', description: '管理部门员工、审批' },
  { code: RoleCode.FINANCE, name: '财务专员', description: '管理薪资核算' },
  { code: RoleCode.EMPLOYEE, name: '普通员工', description: '查看个人信息' },
];
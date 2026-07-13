/**
 * 认证相关接口
 * 负责人：成员 A
 */

import { ROLE_PERMISSIONS } from '@/constants/permissions';
import { ROLE_LIST, RoleCode } from '@/types/user';
import request from '@/utils/request';

// ============ 类型定义 ============

export interface LoginRequest {
  username: string;
  password: string;
  role?: string; // 开发阶段选择的角色
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  roleCode: string;
  permissions: string[];
}

export interface CurrentUser {
  userId: number;
  username: string;
  nickname: string;
  realName?: string;
  deptId?: number;
  deptName?: string;
  roleCode: string;
  roleName?: string;
  permissions: string[];
}

// ============ 接口定义 ============

const ROLE_NAME_MAP = ROLE_LIST.reduce<Record<string, string>>((map, role) => {
  map[role.code] = role.name;
  return map;
}, {});

/**
 * 构建开发阶段本地登录用户
 */
function buildLocalUser(data: LoginRequest): CurrentUser {
  const roleCode = data.role || RoleCode.ADMIN;
  return {
    userId: 1,
    username: data.username,
    nickname: data.username === 'admin' ? '系统管理员' : data.username,
    realName: data.username === 'admin' ? '系统管理员' : data.username,
    deptId: 1,
    deptName: '人力资源部',
    roleCode,
    roleName: ROLE_NAME_MAP[roleCode] || '系统管理员',
    permissions: ROLE_PERMISSIONS[roleCode] || ROLE_PERMISSIONS.ADMIN,
  };
}

/**
 * 用户登录
 */
export async function login(data: LoginRequest): Promise<LoginResult> {
  if (!data.username || !data.password) {
    throw new Error('请输入用户名和密码');
  }

  const localUser = buildLocalUser(data);
  const token = `try-frontend-token-${Date.now()}`;
  localStorage.setItem('token', token);
  localStorage.setItem('userInfo', JSON.stringify(localUser));

  return {
    token,
    userId: localUser.userId,
    username: localUser.username,
    nickname: localUser.nickname,
    roleCode: localUser.roleCode,
    permissions: localUser.permissions,
  };
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser(): Promise<CurrentUser> {
  const localUserText = localStorage.getItem('userInfo');
  if (localUserText) {
    return JSON.parse(localUserText) as CurrentUser;
  }

  return request.get('/auth/current-user');
}

/**
 * 用户登出
 */
export async function logout(): Promise<void> {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
}

/**
 * 认证相关接口
 * 负责人：成员 A
 */

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

/**
 * 用户登录
 */
export async function login(data: LoginRequest): Promise<LoginResult> {
  return request.post('/auth/login', data);
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser(): Promise<CurrentUser> {
  return request.get('/auth/current-user');
}

/**
 * 用户登出
 */
export async function logout(): Promise<void> {
  return request.post('/auth/logout');
}
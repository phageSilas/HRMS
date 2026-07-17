/**
 * 系统管理模块 API 服务
 * 包含：用户管理、角色管理、菜单管理
 */

import request from '@/utils/request';

// ============ 用户管理 ============

export interface UserQueryParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
  deptId?: number;
}

export interface UserItem {
  id: number;
  username: string;
  realName: string;
  phone: string;
  email: string;
  status: number;
  roleIds?: number[];
  roleNames?: string[];
  lastLoginTime?: string;
  createTime?: string;
}

export interface UserPageResult {
  records: UserItem[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

/**
 * 获取用户列表
 */
export function getUserList(params: UserQueryParams): Promise<UserPageResult> {
  return request.get('/api/v1/users', { params });
}

/**
 * 创建用户
 */
export function createUser(data: Partial<UserItem> & { password: string; roleIds?: number[] }): Promise<any> {
  return request.post('/api/v1/users', data);
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: Partial<UserItem> & { roleIds?: number[] }): Promise<any> {
  return request.put(`/api/v1/users/${id}`, data);
}

/**
 * 删除用户
 */
export function deleteUser(id: number): Promise<any> {
  return request.delete(`/api/v1/users/${id}`);
}

/**
 * 重置密码
 */
export function resetPassword(id: number): Promise<{ newPassword: string }> {
  return request.put(`/api/v1/users/${id}/reset-pwd`);
}

// ============ 角色管理 ============

export interface RoleItem {
  id: number;
  roleName: string;
  roleCode: string;
  dataScope: number;
  sortNo: number;
  status: number;
  menuIds?: number[];
  createTime?: string;
}

/**
 * 获取角色列表
 */
export function getRoleList(params?: { keyword?: string; status?: number }): Promise<RoleItem[]> {
  return request.get('/api/v1/roles', { params });
}

/**
 * 创建角色
 */
export function createRole(data: Partial<RoleItem>): Promise<any> {
  return request.post('/api/v1/roles', data);
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: Partial<RoleItem>): Promise<any> {
  return request.put(`/api/v1/roles/${id}`, data);
}

/**
 * 删除角色
 */
export function deleteRole(id: number): Promise<any> {
  return request.delete(`/api/v1/roles/${id}`);
}

/**
 * 分配角色菜单权限
 */
export function assignRoleMenus(roleId: number, menuIds: number[]): Promise<any> {
  return request.post(`/api/v1/roles/${roleId}/menus`, { menuIds });
}

// ============ 菜单管理 ============

export interface MenuItem {
  id: number;
  menuName: string;
  menuType: number; // 0-目录 1-菜单 2-按钮
  path: string;
  component: string;
  permission: string;
  icon: string;
  parentId: number;
  sortNo: number;
  status: number;
  createTime?: string;
}

/**
 * 获取菜单列表
 */
export function getMenuList(params?: { keyword?: string; status?: number }): Promise<MenuItem[]> {
  return request.get('/api/v1/menus', { params });
}

/**
 * 获取菜单树
 */
export function getMenuTree(): Promise<MenuItem[]> {
  return request.get('/api/v1/menus/tree');
}

/**
 * 创建菜单
 */
export function createMenu(data: Partial<MenuItem>): Promise<any> {
  return request.post('/api/v1/menus', data);
}

/**
 * 更新菜单
 */
export function updateMenu(id: number, data: Partial<MenuItem>): Promise<any> {
  return request.put(`/api/v1/menus/${id}`, data);
}

/**
 * 删除菜单
 */
export function deleteMenu(id: number): Promise<any> {
  return request.delete(`/api/v1/menus/${id}`);
}

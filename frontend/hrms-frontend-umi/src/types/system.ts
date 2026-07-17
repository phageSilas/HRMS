/**
 * 系统管理模块类型定义
 */

// 用户
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

export interface UserQueryParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
  deptId?: number;
}

export interface UserPageResult {
  records: UserItem[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

// 角色
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

// 菜单
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

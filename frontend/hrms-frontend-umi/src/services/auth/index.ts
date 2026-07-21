/**
 * 认证相关接口
 * 负责人：成员 A
 */

import { transformMenus } from '@/types/menu';
import { ROLE_PERMISSIONS } from '@/constants/permissions';
import { ROLE_LIST, RoleCode } from '@/types/user';
import request from '@/utils/request';

// ============ 类型定义 ============

export interface LoginRequest {
  username: string;
  password: string;
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
  /** 用户菜单树 */
  menus?: any[];
}

// ============ 接口定义 ============

const ROLE_NAME_MAP = ROLE_LIST.reduce<Record<string, string>>((map, role) => {
  map[role.code] = role.name;
  return map;
}, {});

/**
 * 构建开发阶段本地登录用户（仅在网络不可达时降级使用）
 */
function buildLocalUser(data: LoginRequest): CurrentUser {
  return {
    userId: 1,
    username: data.username,
    nickname: data.username === 'admin' ? '系统管理员' : data.username,
    realName: data.username === 'admin' ? '系统管理员' : data.username,
    deptId: 1,
    deptName: '人力资源部',
    roleCode: RoleCode.ADMIN,
    roleName: ROLE_NAME_MAP[RoleCode.ADMIN] || '系统管理员',
    permissions: ROLE_PERMISSIONS[RoleCode.ADMIN] || [],
  };
}

/**
 * 用户登录
 */
export async function login(data: LoginRequest): Promise<LoginResult> {
  if (!data.username || !data.password) {
    throw new Error('请输入用户名和密码');
  }

  try {
    // 调用后端真实登录接口，获取 JWT Token
    const loginResult: any = await request.post('/api/v1/auth/login', {
      username: data.username,
      password: data.password,
    });

    const { token, userInfo } = loginResult;
    const roleCode = userInfo.roles?.[0]?.roleCode || RoleCode.ADMIN;

    // 存储 token
    localStorage.setItem('token', token);

    // 登录成功后，调用 getCurrentUser 获取完整的用户信息（包含菜单）
    const currentUser = await getCurrentUser();

    // 存储完整用户信息到本地缓存
    localStorage.setItem('userInfo', JSON.stringify(currentUser));

    return {
      token,
      userId: currentUser.userId,
      username: currentUser.username,
      nickname: currentUser.nickname,
      roleCode: currentUser.roleCode,
      permissions: currentUser.permissions,
    };
  } catch (error: any) {
    // 仅在网络不可达时降级到本地登录（开发/演示环境）
    // 业务错误（密码错误、账号锁定等）由拦截器抛出 plain Error，直接向上传递
    const networkCodes = ['ECONNREFUSED', 'ERR_NETWORK', 'ECONNABORTED', 'ERR_CONNECTION_REFUSED', 'ETIMEDOUT'];
    if (error?.code && networkCodes.includes(error.code)) {
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

    // 业务错误：直接抛出
    throw error;
  }
}

/**
 * 获取当前用户信息
 * 注意：此函数总是从后端获取最新数据，不使用本地缓存
 */
export async function getCurrentUser(): Promise<CurrentUser> {
  // 从后端获取
  const result: any = await request.get('/api/v1/auth/current-user');

  const roleCode = result.roles?.[0]?.roleCode || RoleCode.ADMIN;

  // 转换菜单数据
  const menus = result.menus ? transformMenus(result.menus) : undefined;

  return {
    userId: result.id,
    username: result.username,
    nickname: result.realName || result.username,
    realName: result.realName,
    deptId: result.deptId,
    deptName: result.deptName,
    roleCode,
    roleName: result.roles?.[0]?.roleName || ROLE_NAME_MAP[roleCode] || '系统管理员',
    permissions: result.permissions || [],
    menus,
  };
}

/**
 * 用户登出
 */
export async function logout(): Promise<void> {
  try {
    // 调用后端登出接口，使 Token 失效（后端加入黑名单）
    await request.post('/api/v1/auth/logout');
  } catch (error) {
    // 静默处理：即使后端调用失败，也要确保本地清理
    console.warn('调用后端登出接口失败:', error);
  } finally {
    // 清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
  }
}

/**
 * 运行时配置
 * https://umijs.org/docs/api/runtime-config
 */

import { getCurrentUser } from '@/services/auth';
import type { UserInfo } from '@/types/user';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { message } from 'antd';

// 全局状态类型
export interface InitialState {
  currentUser?: UserInfo;
  loading?: boolean;
}

/**
 * 全局初始化数据配置
 * 用于 Layout 用户信息和权限初始化
 */
export async function getInitialState(): Promise<InitialState> {
  const { pathname } = window.location;
  const token = localStorage.getItem('token');

  if (token) {
    try {
      const result = await getCurrentUser();
      const userInfo: UserInfo = {
        userId: result.userId,
        username: result.username,
        nickname: result.nickname || result.username,
        deptId: result.deptId,
        deptName: result.deptName,
        roleCode: result.roleCode,
        roleName: result.roleName,
        permissions: result.permissions,
      };
      return {
        currentUser: userInfo,
        loading: false,
      };
    } catch (error) {
      // 获取用户信息失败，跳转登录页
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      if (pathname !== '/login') {
        history.push('/login');
      }
    }
  } else if (pathname !== '/login') {
    // 非登录页没有 token，跳转登录页
    history.push('/login');
  }

  return {
    loading: false,
  };
}

/**
 * Layout 配置
 */
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    logo: 'https://img.alicdn.com/tfs/TB1YHEpwUT1gK0jSZFhXXaAtVXa-28-27.svg',
    title: 'HRMS 人资管理系统',
    menu: {
      locale: false,
    },
    // 用户信息显示
    avatar:
      initialState?.currentUser?.avatar ||
      'https://gw.alipayobjects.com/zos/antfincdn/efFD%24gQ%24g/LC_ChangX.png',
    name:
      initialState?.currentUser?.nickname ||
      initialState?.currentUser?.username ||
      '未登录',
    // 退出登录
    onMenuClick: ({ key }) => {
      if (key === 'logout') {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        message.success('已退出登录');
        history.push('/login');
      }
    },
  };
};

/**
 * 请求配置
 */
export const request = {
  timeout: 10000,
  errorConfig: {
    adaptor: (resData: { code: number; message: string }) => {
      return {
        success: resData.code === 20000,
        errorMessage: resData.message,
        errorCode: resData.code,
      };
    },
  },
};

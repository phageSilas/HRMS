/**
 * 运行时配置
 * https://umijs.org/docs/api/runtime-config
 */

import LayoutFrame from '@/components/AppShell/LayoutFrame';
import { getCurrentUser } from '@/services/auth';
import type { UserInfo } from '@/types/user';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { message } from 'antd';
import React from 'react';
import './global.less';

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
  const localUserText = localStorage.getItem('userInfo');

  // 优先从本地缓存恢复用户信息，避免每次导航都调后端 API
  if (token && localUserText) {
    try {
      const parsed = JSON.parse(localUserText) as UserInfo;
      return { currentUser: parsed, loading: false };
    } catch {
      // 缓存数据损坏，走下方逻辑重新获取
    }
  }

  // 有 token 但无本地缓存时，再调后端接口获取
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
      // 同步缓存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      return {
        currentUser: userInfo,
        loading: false,
      };
    } catch {
      // 获取用户信息失败（后端未就绪或 token 不合法）
      // 不清除 token 也不跳转：让请求拦截器在具体 API 调用时处理
      // 页面组件内的鉴权逻辑会自行处理未登录状态
    }
  } else if (pathname !== '/login') {
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
  const avatarUrl =
    initialState?.currentUser?.avatar ||
    'https://gw.alipayobjects.com/zos/antfincdn/efFD%24gQ%24g/LC_ChangX.png';
  const displayName =
    initialState?.currentUser?.nickname ||
    initialState?.currentUser?.username ||
    '未登录';

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    message.success('已退出登录');
    history.push('/login');
  };

  return {
    logo: false,
    title: false,
    layout: 'side',
    navTheme: 'light',
    fixedHeader: true,
    fixSiderbar: true,
    className: 'hrms-art-layout',
    menu: {
      locale: false,
    },
    menuHeaderRender: () =>
      React.createElement(
        'div',
        { className: 'hrms-art-brand' },
        React.createElement('span', { className: 'hrms-art-logo' }),
        React.createElement(
          'span',
          { className: 'hrms-art-brand-text' },
          React.createElement('strong', null, 'HRMS'),
          React.createElement('small', null, '管理平台')
        )
      ),
    childrenRender: (children) =>
      React.createElement(
        LayoutFrame,
        {
          currentUser: initialState?.currentUser,
          avatarUrl,
          displayName,
          onLogout: handleLogout,
        },
        children
      ),
    onMenuClick: ({ key }) => {
      if (key === 'logout') {
        handleLogout();
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

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
  /** 用户菜单树 */
  menus?: UserInfo['menus'];
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
      return {
        currentUser: parsed,
        menus: parsed.menus,  // 从缓存中恢复菜单
        loading: false
      };
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
        menus: result.menus,
      };
      // 同步缓存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      return {
        currentUser: userInfo,
        menus: result.menus,
        loading: false,
      };
    } catch {
      // 获取用户信息失败（后端未就绪或 token 不合法）
      // 清除 token 并跳转登录页
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      // 注意：这里不直接跳转，让 render 或 onRouteChange 处理
    }
  }

  return {
    loading: false,
  };
}

/**
 * 自定义渲染 - 在应用渲染前进行权限拦截
 * 这是最早能拦截路由重定向的时机
 */
export function render(oldRender: () => void) {
  const token = localStorage.getItem('token');
  const pathname = window.location.pathname;

  // 未登录且访问根路径时，阻止默认渲染（默认会重定向到 /home）
  // 强制跳转到登录页
  if (!token && (pathname === '/' || pathname === '/home')) {
    history.push('/login');
    // 必须调用 oldRender，否则页面不会渲染
    oldRender();
    return;
  }

  oldRender();
}

/**
 * 路由变化监听 - 未登录时强制跳转登录页
 * 注意：onRouteChange 在路由匹配后执行，此时重定向已经发生
 * 因此需要在 render 阶段进行权限拦截
 */
export function onRouteChange({ location }: { location: { pathname: string } }) {
  const token = localStorage.getItem('token');
  const isLoginPage = location.pathname === '/login';

  // 未登录且不在登录页，强制跳转登录页
  if (!token && !isLoginPage) {
    history.push('/login');
  }
}

/**
 * Layout 配置
 */
export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
  const avatarUrl = initialState?.currentUser?.avatar;
  const displayName =
    initialState?.currentUser?.nickname ||
    initialState?.currentUser?.username ||
    '未登录';

  const handleLogout = async () => {
    // 调用 logout 服务，同步清除后端 Token 黑名单
    const { logout } = await import('@/services/auth');
    await logout();
    message.success('已退出登录');
    // 清除全局状态
    setInitialState?.({ loading: false });
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
    // 动态菜单：从后端获取的菜单数据渲染侧边栏
    // menuDataRender 会替换掉默认从路由生成的菜单
    menuDataRender: (menuList) => {
      // 如果有从后端获取的菜单数据，则使用后端数据
      if (initialState?.menus && initialState.menus.length > 0) {
        return initialState.menus;
      }
      // 否则返回空数组
      return [];
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
 * 注意：前端使用自定义 axios 实例（@/utils/request），已在拦截器中统一处理 Result<T> 解包
 * 因此不再配置 umi 的 request 运行时配置，避免 useRequest 二次解包导致数据丢失
 */

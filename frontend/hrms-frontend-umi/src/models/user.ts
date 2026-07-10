/**
 * 用户状态模型
 */

import { useState, useCallback } from 'react';
import type { UserInfo, LoginRequest, LoginResult } from '@/types/user';
import { login as loginApi, getCurrentUser, logout as logoutApi } from '@/services/auth';
import { history, useModel } from '@umijs/max';
import { message } from 'antd';

export default function useUserModel() {
  const [currentUser, setCurrentUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(false);

  /**
   * 登录
   */
  const login = useCallback(async (params: LoginRequest): Promise<boolean> => {
    setLoading(true);
    try {
      const result: LoginResult = await loginApi(params);

      // 存储 Token
      localStorage.setItem('token', result.token);

      // 设置用户信息
      const userInfo: UserInfo = {
        userId: result.userId,
        username: result.username,
        nickname: result.nickname,
        roleCode: result.roleCode,
        permissions: result.permissions,
      };
      setCurrentUser(userInfo);
      localStorage.setItem('userInfo', JSON.stringify(userInfo));

      return true;
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : '登录失败';
      message.error(errorMessage);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 获取当前用户信息
   */
  const fetchCurrentUser = useCallback(async (): Promise<UserInfo | null> => {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }

    setLoading(true);
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
      setCurrentUser(userInfo);
      return userInfo;
    } catch (error) {
      // 获取用户信息失败，清除 token
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 退出登录
   */
  const logout = useCallback(async () => {
    try {
      await logoutApi();
    } catch {
      // 忽略退出接口错误
    } finally {
      // 清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      setCurrentUser(null);

      // 跳转登录页
      history.push('/login');
    }
  }, []);

  /**
   * 从本地存储恢复用户信息
   */
  const restoreUserFromStorage = useCallback((): UserInfo | null => {
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr) as UserInfo;
        setCurrentUser(userInfo);
        return userInfo;
      } catch {
        return null;
      }
    }
    return null;
  }, []);

  return {
    currentUser,
    loading,
    login,
    logout,
    fetchCurrentUser,
    restoreUserFromStorage,
    setCurrentUser,
  };
}
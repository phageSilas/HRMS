/**
 * 登录认证 Mock
 * 支持角色切换（开发环境）
 */

import { Request, Response } from 'express';
import type { LoginResult } from '@/types/user';

// 角色权限映射
const rolePermissions: Record<string, string[]> = {
  ADMIN: ['system', 'employee', 'process', 'attendance', 'salary', 'approval', 'mycenter'],
  HR: ['employee', 'process', 'attendance', 'salary', 'approval', 'mycenter'],
  MANAGER: ['employee', 'process', 'attendance', 'approval', 'mycenter'],
  FINANCE: ['salary', 'approval', 'mycenter'],
  EMPLOYEE: ['mycenter'],
};

// 角色名称映射
const roleNames: Record<string, string> = {
  ADMIN: '系统管理员',
  HR: 'HR 专员',
  MANAGER: '部门主管',
  FINANCE: '财务专员',
  EMPLOYEE: '普通员工',
};

// 模拟用户数据
const mockUsers = [
  { id: 1, username: 'admin', password: 'admin123', nickname: '管理员' },
  { id: 2, username: 'hr', password: 'hr123', nickname: 'HR专员' },
  { id: 3, username: 'manager', password: 'manager123', nickname: '部门主管' },
  { id: 4, username: 'finance', password: 'finance123', nickname: '财务专员' },
  { id: 5, username: 'employee', password: 'employee123', nickname: '普通员工' },
];

// 存储当前登录用户的角色
let currentRole = 'ADMIN';
let currentUserId = 1;
let currentUsername = 'admin';

export default {
  // 登录接口
  'POST /auth/login': (req: Request, res: Response) => {
    const { username, password, role } = req.body;

    // 模拟延迟
    setTimeout(() => {
      // 查找用户
      const user = mockUsers.find((u) => u.username === username);

      // 验证用户名密码（开发环境宽松验证）
      if (!user || (password && password !== user.password && password !== '123456')) {
        return res.json({
          code: 40101,
          message: '用户名或密码错误',
          data: null,
        });
      }

      // 开发环境：允许选择任意角色
      const selectedRole = role || 'ADMIN';
      currentRole = selectedRole;
      currentUserId = user?.id || 1;
      currentUsername = username || 'admin';

      const result: LoginResult = {
        token: `mock-token-${selectedRole}-${Date.now()}`,
        userId: currentUserId,
        username: currentUsername,
        nickname: user?.nickname || roleNames[selectedRole],
        roleCode: selectedRole,
        permissions: rolePermissions[selectedRole],
      };

      res.json({
        code: 0,
        message: 'success',
        data: result,
      });
    }, 300);
  },

  // 获取当前用户信息
  'GET /auth/current-user': (req: Request, res: Response) => {
    setTimeout(() => {
      res.json({
        code: 0,
        message: 'success',
        data: {
          userId: currentUserId,
          username: currentUsername,
          nickname: roleNames[currentRole],
          realName: roleNames[currentRole],
          deptId: 1,
          deptName: '总部',
          roleCode: currentRole,
          roleName: roleNames[currentRole],
          permissions: rolePermissions[currentRole],
        },
      });
    }, 200);
  },

  // 退出登录
  'POST /auth/logout': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: null,
    });
  },
};
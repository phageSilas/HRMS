/**
 * 登录页面
 * 支持用户名密码登录和开发阶段角色选择
 */

import React, { useState } from 'react';
import { Form, Input, Button, Select, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import type { LoginRequest } from '@/types/user';
import { ROLE_LIST, RoleCode } from '@/types/user';
import styles from './index.less';

const { Option } = Select;

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const { refresh } = useModel('@@initialState');

  // 是否为开发环境
  const isDev = process.env.NODE_ENV === 'development';

  /**
   * 处理登录
   */
  const handleLogin = async (values: LoginRequest) => {
    setLoading(true);
    try {
      // 调用登录接口
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(values),
      });

      const result = await response.json();

      if (result.code === 0) {
        // 存储 Token
        localStorage.setItem('token', result.data.token);
        localStorage.setItem('userInfo', JSON.stringify({
          userId: result.data.userId,
          username: result.data.username,
          nickname: result.data.nickname,
          roleCode: result.data.roleCode,
          permissions: result.data.permissions,
        }));

        message.success('登录成功');

        // 刷新全局状态
        await refresh();

        // 跳转首页
        history.push('/home');
      } else {
        message.error(result.message || '登录失败');
      }
    } catch (error) {
      message.error('登录失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <Card className={styles.card}>
          <div className={styles.header}>
            <h1>HRMS 人资管理系统</h1>
            <p>Human Resource Management System</p>
          </div>

          <Form
            form={form}
            onFinish={handleLogin}
            initialValues={{
              username: 'admin',
              password: 'admin123',
              role: RoleCode.ADMIN,
            }}
            size="large"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="用户名"
                autoComplete="username"
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="密码"
                autoComplete="current-password"
              />
            </Form.Item>

            {/* 开发环境：角色选择 */}
            {isDev && (
              <Form.Item
                name="role"
                rules={[{ required: true, message: '请选择角色' }]}
              >
                <Select placeholder="选择角色（开发环境）">
                  {ROLE_LIST.map((role) => (
                    <Option key={role.code} value={role.code}>
                      {role.name} - {role.description}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                block
              >
                登录
              </Button>
            </Form.Item>
          </Form>

          {isDev && (
            <div className={styles.devTip}>
              <p>开发环境提示：</p>
              <ul>
                <li>任意用户名密码均可登录</li>
                <li>选择不同角色可测试权限控制</li>
                <li>生产环境将隐藏角色选择</li>
              </ul>
            </div>
          )}
        </Card>

        <div className={styles.footer}>
          <p>© 2024 HRMS 人资管理系统</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
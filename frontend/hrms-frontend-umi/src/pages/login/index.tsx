/**
 * 登录页面
 * 支持用户名密码登录和开发阶段角色选择
 */

import { login } from '@/services/auth';
import type { LoginRequest } from '@/types/user';
import { ROLE_LIST, RoleCode } from '@/types/user';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Button, Card, Form, Input, Select, message } from 'antd';
import React, { useState } from 'react';
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
      await login(values);
      message.success('登录成功');
      await refresh();
      history.push('/home');
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
                data-testid="login-submit"
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

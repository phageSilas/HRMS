/**
 * 登录页面
 * 保留 HRMS 登录校验逻辑，仅重塑登录入口视觉。
 */

import { login } from '@/services/auth';
import type { LoginRequest } from '@/types/user';
import { LockOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Button, Checkbox, Form, Input, message } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import styles from './index.less';

type LoginPointerStyle = React.CSSProperties & {
  '--pointer-x': string;
  '--pointer-y': string;
  '--shift-x': string;
  '--shift-y': string;
};

const defaultPointerStyle: LoginPointerStyle = {
  '--pointer-x': '50%',
  '--pointer-y': '44%',
  '--shift-x': '0px',
  '--shift-y': '0px',
};

type VantaEffect = {
  destroy: () => void;
};

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [pointerStyle, setPointerStyle] = useState<LoginPointerStyle>(defaultPointerStyle);
  const vantaRef = useRef<HTMLDivElement | null>(null);
  const [form] = Form.useForm();
  const { refresh } = useModel('@@initialState');

  useEffect(() => {
    let effect: VantaEffect | null = null;
    let disposed = false;

    const setupVanta = async () => {
      if (!vantaRef.current || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        return;
      }

      const [{ default: WAVES }, THREE] = await Promise.all([
        import('vanta/dist/vanta.waves.min'),
        import('three'),
      ]);

      if (disposed || !vantaRef.current) {
        return;
      }

      effect = WAVES({
        el: vantaRef.current,
        THREE,
        mouseControls: true,
        touchControls: true,
        gyroControls: false,
        minHeight: 200,
        minWidth: 200,
        scale: 1,
        scaleMobile: 1,
        color: 0x0a6fd6,
        shininess: 44,
        waveHeight: 16,
        waveSpeed: 0.72,
        zoom: 0.86,
      });
    };

    setupVanta();

    return () => {
      disposed = true;
      effect?.destroy();
    };
  }, []);

  /**
   * 根据鼠标位置驱动背景视差
   */
  const handlePointerMove = (event: React.PointerEvent<HTMLElement>) => {
    const { currentTarget, clientX, clientY } = event;
    const rect = currentTarget.getBoundingClientRect();
    const xRatio = (clientX - rect.left) / rect.width;
    const yRatio = (clientY - rect.top) / rect.height;
    const x = Math.max(0, Math.min(1, xRatio));
    const y = Math.max(0, Math.min(1, yRatio));

    setPointerStyle({
      '--pointer-x': `${Math.round(x * 100)}%`,
      '--pointer-y': `${Math.round(y * 100)}%`,
      '--shift-x': `${((x - 0.5) * 34).toFixed(1)}px`,
      '--shift-y': `${((y - 0.5) * 28).toFixed(1)}px`,
    });
  };

  /**
   * 处理登录提交
   */
  const handleLogin = async (values: LoginRequest & { remember?: boolean }) => {
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
    <main
      className={styles.container}
      style={pointerStyle}
      onPointerMove={handlePointerMove}
      onPointerLeave={() => setPointerStyle(defaultPointerStyle)}
    >
      <div ref={vantaRef} className={styles.vantaLayer} aria-hidden="true" />
      <div className={styles.backgroundGrid} aria-hidden="true" />
      <div className={styles.backgroundWave} aria-hidden="true" />
      <section className={styles.stage}>
        <h1 className={styles.pageTitle}>HRMS管理平台</h1>
        <div className={styles.loginBox}>
          <div className={styles.formPanel}>
            <div className={styles.formHeader}>
              <span className={styles.formEyebrow}>Enterprise access</span>
              <h2>用户登录</h2>
              <p>请输入管理员分配的账号密码进入内网人资系统。</p>
            </div>

            <Form
              form={form}
              onFinish={handleLogin}
              initialValues={{
                username: 'admin',
                password: 'admin123',
                remember: true,
              }}
              size="large"
              layout="vertical"
              className={styles.loginForm}
            >
              <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                <Input
                  prefix={<UserOutlined />}
                  placeholder="用户名"
                  autoComplete="username"
                  data-testid="login-username"
                />
              </Form.Item>

              <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="密码"
                  autoComplete="current-password"
                  data-testid="login-password"
                />
              </Form.Item>

              <div className={styles.formMeta}>
                <Form.Item name="remember" valuePropName="checked" noStyle>
                  <Checkbox>记住登录状态</Checkbox>
                </Form.Item>
              </div>

              <Form.Item className={styles.submitItem}>
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
          </div>

          <aside className={styles.brandPanel}>
            <div className={styles.brandMark}>
              <SafetyCertificateOutlined />
            </div>
            <h2>HRMS管理平台</h2>
            <p>统一管理员工档案、组织异动、考勤协同与薪资流程，服务企业内部人资运营。</p>
            <div className={styles.brandNotes}>
              <span>内网访问</span>
              <span>统一认证</span>
              <span>权限管控</span>
            </div>
          </aside>
        </div>
      </section>
    </main>
  );
};

export default LoginPage;

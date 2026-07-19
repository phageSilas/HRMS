/**
 * 账号安全页面
 * 修改密码 + 绑定/解绑手机 + 登录日志
 */

import {
  KeyOutlined,
  LockOutlined,
  PhoneOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  message,
  Modal,
  Space,
  Table,
  Tag,
  Typography,
  Spin,
} from 'antd';
import React, { useEffect, useState } from 'react';
import { changePassword, bindPhone, getLoginLogs, unbindPhone } from '@/services/profile';
import type { LoginLogVO, PasswordChangeRequest, PhoneBindRequest } from '@/services/profile';

const { Text, Title } = Typography;

// ============ 密码复杂度校验 ============

const PASSWORD_RULES = [
  { required: true, message: '请输入密码' },
  { min: 8, message: '密码至少8位' },
  {
    pattern: /^(?![a-zA-Z]+$)(?!\d+$)(?![^\da-zA-Z]+$).{8,}$/,
    message: '需包含大小写字母、数字、特殊字符中至少3种',
  },
];

// ============ 页面组件 ============

const ProfileSecurityPage: React.FC = () => {
  // 弹窗状态
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [phoneModalOpen, setPhoneModalOpen] = useState(false);

  // 表单实例
  const [passwordForm] = Form.useForm();
  const [phoneForm] = Form.useForm();

  // 登录日志（使用 useState + useEffect + 直接调 API，隔离 useRequest 问题）
  const [loginLogs, setLoginLogs] = useState<LoginLogVO[]>([]);
  const [logLoading, setLogLoading] = useState(true);
  const [logError, setLogError] = useState<string | null>(null);

  const fetchLoginLogs = async () => {
    console.log('[DEBUG] fetchLoginLogs 被调用');
    setLogLoading(true);
    setLogError(null);
    try {
      const result = await getLoginLogs();
      console.log('[DEBUG] API 返回结果:', result);
      console.log('[DEBUG] 数据类型:', typeof result, '，是否数组:', Array.isArray(result), '，长度:', (result as any)?.length);
      if (Array.isArray(result)) {
        console.log('[DEBUG] 第一条数据:', result[0]);
        setLoginLogs(result as LoginLogVO[]);
      } else {
        console.warn('[DEBUG] 返回数据不是数组:', result);
        setLoginLogs([]);
      }
    } catch (err: any) {
      console.error('[DEBUG] 请求失败:', err);
      console.error('[DEBUG] 错误消息:', err?.message);
      console.error('[DEBUG] 错误堆栈:', err?.stack);
      setLogError(err?.message || '请求失败');
      setLoginLogs([]);
    } finally {
      setLogLoading(false);
      console.log('[DEBUG] fetchLoginLogs 完成，loginLogs 状态:', loginLogs.length, '条');
    }
  };

  useEffect(() => {
    console.log('[DEBUG] useEffect 触发，开始 fetchLoginLogs');
    fetchLoginLogs();
  }, []);

  // ============ 修改密码 ============

  const handlePasswordSubmit = async () => {
    try {
      const values = await passwordForm.validateFields();
      if (values.newPassword !== values.confirmPassword) {
        message.error('两次输入的新密码不一致');
        return;
      }
      const payload: PasswordChangeRequest = {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      };
      await changePassword(payload);
      message.success('密码修改成功');
      setPasswordModalOpen(false);
      passwordForm.resetFields();
    } catch {
      // 静默处理
    }
  };

  // ============ 绑定手机 ============

  const handleUnbind = () => {
    Modal.confirm({
      title: '确认解绑手机',
      content: '解绑手机号后，将无法使用手机号登录和找回密码。确定要解绑吗？',
      okText: '确认解绑',
      cancelText: '取消',
      onOk: async () => {
        try {
          await unbindPhone();
          message.success('手机号已解绑');
        } catch {
          // 静默处理
        }
      },
    });
  };

  const handlePhoneSubmit = async () => {
    try {
      const values = await phoneForm.validateFields();
      const payload: PhoneBindRequest = {
        phone: values.phone,
        password: values.password,
      };
      await bindPhone(payload);
      message.success('手机更换成功');
      setPhoneModalOpen(false);
      phoneForm.resetFields();
    } catch (error: any) {
      if (error?.response?.data?.message) {
        message.error(error.response.data.message);
      } else if (error instanceof Error) {
        message.error(error.message);
      }
    }
  };

  // ============ 登录日志表格列 ============

  const logColumns = [
    {
      title: '登录时间',
      dataIndex: 'loginTime',
      key: 'loginTime',
      width: 180,
      render: (t: string) => t || '-',
    },
    { title: 'IP 地址', dataIndex: 'ip', key: 'ip', width: 140 },
    {
      title: '设备信息',
      key: 'device',
      width: 200,
      render: (_: any, record: LoginLogVO) => {
        const parts: string[] = [];
        if (record.browser) parts.push(record.browser);
        if (record.os) parts.push(record.os);
        return parts.length > 0 ? parts.join(' / ') : '-';
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (s: number) =>
        s === 1 ? (
          <Tag color="success">成功</Tag>
        ) : (
          <Tag color="error">失败</Tag>
        ),
    },
    {
      title: '错误信息',
      dataIndex: 'errorMsg',
      key: 'errorMsg',
      ellipsis: true,
      render: (t: string) => t || '-',
    },
  ];

  // ============ 渲染 ============

  return (
    <PageContainer>
      {/* 安全设置列表 */}
      <Card bordered={false} style={{ marginBottom: 16 }}>
        <Descriptions column={1} labelStyle={{ width: 120 }}>
          <Descriptions.Item label="登录密码">
            <Space>
              <KeyOutlined style={{ color: '#1677ff' }} />
              <Text>已设置</Text>
              <Button type="link" onClick={() => setPasswordModalOpen(true)}>
                修改密码
              </Button>
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="绑定手机">
            <Space>
              <PhoneOutlined style={{ color: '#52c41a' }} />
              <Text>已绑定</Text>
              <Button type="link" onClick={() => setPhoneModalOpen(true)}>
                更换手机
              </Button>
              <Button type="link" danger onClick={handleUnbind}>
                解绑
              </Button>
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="账号安全">
            <Space>
              <SafetyCertificateOutlined style={{ color: '#faad14' }} />
              <Text type="secondary">建议定期修改密码以保障账号安全</Text>
            </Space>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 登录日志 */}
      <Card bordered={false} title={`登录日志${loginLogs.length > 0 ? `（${loginLogs.length}条）` : ''}`}>
        {logError && (
          <div style={{ color: '#ff4d4f', marginBottom: 12, padding: 8, background: '#fff2f0', borderRadius: 4 }}>
            请求出错：{logError}
          </div>
        )}
        {logLoading ? (
          <Spin />
        ) : (
          <Table
            dataSource={loginLogs}
            columns={logColumns}
            rowKey={(_, index) => index}
            pagination={{ pageSize: 10, showTotal: (t) => `共 ${t} 条` }}
            locale={{ emptyText: '暂无登录日志' }}
          />
        )}
      </Card>

      {/* 修改密码弹窗 */}
      <Modal
        title="修改密码"
        open={passwordModalOpen}
        onOk={handlePasswordSubmit}
        onCancel={() => {
          setPasswordModalOpen(false);
          passwordForm.resetFields();
        }}
        destroyOnClose
      >
        <Form form={passwordForm} layout="vertical">
          <Form.Item
            name="oldPassword"
            label="旧密码"
            rules={[{ required: true, message: '请输入旧密码' }]}
          >
            <Input.Password placeholder="请输入旧密码" />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={PASSWORD_RULES}
          >
            <Input.Password placeholder="8位以上，含大小写字母、数字、特殊字符中至少3种" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请再次输入新密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password placeholder="请再次输入新密码" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 更换手机弹窗 */}
      <Modal
        title="更换手机"
        open={phoneModalOpen}
        onOk={handlePhoneSubmit}
        onCancel={() => {
          setPhoneModalOpen(false);
          phoneForm.resetFields();
        }}
        destroyOnClose
      >
        <Form form={phoneForm} layout="vertical">
          <Form.Item
            name="phone"
            label="新手机号"
            rules={[
              { required: true, message: '请输入新手机号' },
              { pattern: /^1\d{10}$/, message: '请输入正确的手机号格式' },
            ]}
          >
            <Input placeholder="请输入新手机号" maxLength={11} />
          </Form.Item>
          <Form.Item
            name="password"
            label="当前密码"
            rules={[{ required: true, message: '请输入当前密码以验证身份' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入当前密码"
            />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ProfileSecurityPage;

/**
 * 我的档案页面
 *
 * 顶部个人卡片 + 左右两栏布局：
 *   左栏「基本信息」— 只读，底部提示联系 HR
 *   右栏「联系信息」— 查看/编辑态切换，可保存
 */

import {
  EditOutlined,
  SaveOutlined,
  CloseOutlined,
  InfoCircleOutlined,
  UserOutlined,
  MailOutlined,
  HomeOutlined,
  ContactsOutlined,
  PhoneOutlined,
} from '@ant-design/icons';
import {
  Avatar,
  Button,
  Card,
  Col,
  Form,
  Input,
  message,
  Row,
  Spin,
  Space,
  Tag,
  Typography,
} from 'antd';
import React, { useState, useEffect, useCallback } from 'react';
import { getProfile, updateProfile } from '@/services/profile';
import type { ProfileVO, ProfileUpdateRequest } from '@/services/profile';

const { Text, Title } = Typography;
const { TextArea } = Input;

// ============ 工具函数 ============

/** 手机号脱敏：138****0001 */
const maskPhone = (phone?: string): string => {
  if (!phone || phone.length < 7) return phone || '-';
  return phone.slice(0, 3) + '****' + phone.slice(-4);
};

// ============ 页面组件 ============

const ProfileArchivePage: React.FC = () => {
  const [profile, setProfile] = useState<ProfileVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    setFetchError(null);
    try {
      const data = await getProfile();
      setProfile(data);
    } catch (err: any) {
      setFetchError(err?.message || '获取档案信息失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  // ============ 编辑态操作 ============

  /** 进入编辑态 — 回填当前值到表单 */
  const handleStartEdit = () => {
    if (!profile) return;
    form.setFieldsValue({
      email: profile.email || '',
      currentAddress: profile.currentAddress || '',
      emergencyContact: profile.emergencyContact || '',
      emergencyPhone: profile.emergencyPhone || '',
    });
    setEditing(true);
  };

  /** 取消编辑 */
  const handleCancelEdit = () => {
    form.resetFields();
    setEditing(false);
  };

  /** 保存联系信息 */
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const payload: ProfileUpdateRequest = {
        email: values.email || undefined,
        currentAddress: values.currentAddress || undefined,
        emergencyContact: values.emergencyContact || undefined,
        emergencyPhone: values.emergencyPhone || undefined,
      };
      await updateProfile(payload);
      message.success('联系信息已更新');
      setEditing(false);
      fetchProfile();
    } catch (error: any) {
      if (error?.errorFields) return; // 表单校验失败
      message.error(error?.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  // ============ 渲染：加载 & 错误 ============

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!profile) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Text type="warning">
          获取档案信息失败
          {fetchError && (
            <div style={{ marginTop: 8, fontSize: 13, color: '#999' }}>
              错误详情：{fetchError}
            </div>
          )}
        </Text>
      </div>
    );
  }

  // ============ 渲染：顶部个人卡片 ============

  const renderHeaderCard = () => (
    <Card
      bordered={false}
      style={{ marginBottom: 20, borderRadius: 8 }}
    >
      <Row align="middle" gutter={20}>
        <Col>
          <Avatar
            size={72}
            icon={<UserOutlined />}
            style={{ backgroundColor: '#1677ff' }}
          />
        </Col>
        <Col flex="auto">
          <Title level={4} style={{ margin: 0, marginBottom: 6 }}>
            {profile.employeeName || '未知'}
          </Title>
          <Text type="secondary" style={{ display: 'block', marginBottom: 8, fontSize: 13 }}>
            {profile.employeeNo || '-'}
          </Text>
          <Space>
            <Tag color="blue">{profile.deptName || '-'}</Tag>
            <Tag color="green">{profile.employmentStatusDesc || '在职'}</Tag>
          </Space>
        </Col>
      </Row>
    </Card>
  );

  // ============ 渲染：左栏 — 基本信息（只读） ============

  const renderBasicInfo = () => {
    const fields: { label: string; value: string | undefined }[] = [
      { label: '工号', value: profile.employeeNo },
      { label: '部门', value: profile.deptName },
      { label: '职位', value: profile.postName },
      { label: '入职日期', value: profile.hireDate },
      { label: '手机号', value: maskPhone(profile.phone) },
    ];

    return (
      <Card
        title={
          <Space>
            <InfoCircleOutlined style={{ color: '#1677ff' }} />
            <span>基本信息</span>
          </Space>
        }
        bordered={false}
        style={{ borderRadius: 8, height: '100%' }}
      >
        {fields.map((f) => (
          <Row key={f.label} style={{ marginBottom: 16 }} align="middle">
            <Col span={8}>
              <Text type="secondary" style={{ fontSize: 13 }}>
                {f.label}
              </Text>
            </Col>
            <Col span={16}>
              <Text style={{ fontSize: 14 }}>{f.value || '-'}</Text>
            </Col>
          </Row>
        ))}

        {/* 提示联系 HR */}
        <div
          style={{
            marginTop: 20,
            padding: '10px 14px',
            backgroundColor: '#fff7e6',
            borderRadius: 6,
            border: '1px solid #ffd591',
            fontSize: 13,
            color: '#d46b08',
          }}
        >
          <InfoCircleOutlined style={{ marginRight: 6 }} />
          如需修改请联系 HR
        </div>
      </Card>
    );
  };

  // ============ 渲染：右栏 — 联系信息（查看/编辑态） ============

  const renderContactInfo = () => {
    const fields: {
      key: keyof ProfileUpdateRequest;
      label: string;
      icon: React.ReactNode;
      placeholder?: string;
      textArea?: boolean;
    }[] = [
      { key: 'email', label: '邮箱', icon: <MailOutlined />, placeholder: '请输入邮箱' },
      { key: 'currentAddress', label: '现居住地址', icon: <HomeOutlined />, placeholder: '请输入现居住地址', textArea: true },
      { key: 'emergencyContact', label: '紧急联系人', icon: <ContactsOutlined />, placeholder: '请输入紧急联系人姓名' },
      { key: 'emergencyPhone', label: '紧急联系电话', icon: <PhoneOutlined />, placeholder: '请输入紧急联系电话' },
    ];

    return (
      <Card
        title={
          <Space>
            <ContactsOutlined style={{ color: '#1677ff' }} />
            <span>联系信息</span>
          </Space>
        }
        extra={
          editing ? (
            <Space>
              <Button
                size="small"
                icon={<CloseOutlined />}
                onClick={handleCancelEdit}
              >
                取消
              </Button>
              <Button
                type="primary"
                size="small"
                icon={<SaveOutlined />}
                loading={saving}
                onClick={handleSave}
              >
                保存
              </Button>
            </Space>
          ) : (
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={handleStartEdit}
            >
              编辑
            </Button>
          )
        }
        bordered={false}
        style={{ borderRadius: 8, height: '100%' }}
      >
        {editing ? (
          // ====== 编辑态：表单控件 ======
          <Form form={form} layout="vertical">
            {fields.map((f) => (
              <Form.Item
                key={f.key}
                name={f.key}
                label={f.label}
                rules={f.key === 'email'
                  ? [{ type: 'email', message: '请输入正确的邮箱格式' }]
                  : f.key === 'emergencyPhone'
                    ? [{ pattern: /^\d{7,15}$/, message: '请输入正确的联系电话' }]
                    : undefined
                }
              >
                {f.textArea ? (
                  <TextArea rows={2} placeholder={f.placeholder} />
                ) : (
                  <Input placeholder={f.placeholder} />
                )}
              </Form.Item>
            ))}
          </Form>
        ) : (
          // ====== 查看态：纯文本展示 ======
          fields.map((f) => (
            <Row key={f.key} style={{ marginBottom: 16 }} align="middle">
              <Col span={8}>
                <Space size={6}>
                  <Text style={{ color: '#999', fontSize: 13 }}>{f.icon}</Text>
                  <Text type="secondary" style={{ fontSize: 13 }}>
                    {f.label}
                  </Text>
                </Space>
              </Col>
              <Col span={16}>
                <Text style={{ fontSize: 14 }}>
                  {profile[f.key as keyof ProfileVO] || '-'}
                </Text>
              </Col>
            </Row>
          ))
        )}
      </Card>
    );
  };

  // ============ 主渲染 ============

  return (
    <div style={{ padding: 0 }}>
      {renderHeaderCard()}
      <Row gutter={20}>
        <Col xs={24} lg={12}>
          {renderBasicInfo()}
        </Col>
        <Col xs={24} lg={12}>
          {renderContactInfo()}
        </Col>
      </Row>
    </div>
  );
};

export default ProfileArchivePage;

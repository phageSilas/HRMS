/**
 * 我的档案页面
 * 分组展示档案信息，可编辑字段 inline 编辑，锁定字段只读提示
 */

import { EditOutlined, InfoCircleOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  message,
  Modal,
  Spin,
  Tooltip,
  Typography,
} from 'antd';
import React, { useState } from 'react';
import { getProfile, updateProfile } from '@/services/profile';
import type { ProfileUpdateRequest } from '@/services/profile';

const { Text } = Typography;

/** 可编辑字段的实际输入映射 */
const FIELD_LABEL_MAP: Record<string, string> = {
  phone: '手机号',
  email: '邮箱',
  currentAddress: '现居地址',
  emergencyContact: '紧急联系人',
  emergencyPhone: '紧急联系人电话',
};

const ProfileArchivePage: React.FC = () => {
  const { data, loading, refresh } = useRequest(getProfile);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingField, setEditingField] = useState<string>('');
  const [form] = Form.useForm();

  const profile = data;

  // ============ 编辑弹窗提交 ============

  const handleEdit = (field: string) => {
    setEditingField(field);
    form.setFieldsValue({ [field]: profile?.[field as keyof typeof profile] || '' });
    setEditModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const payload: ProfileUpdateRequest = { [editingField]: values[editingField] };
      await updateProfile(payload);
      message.success('更新成功');
      setEditModalOpen(false);
      refresh();
    } catch {
      // 表单校验失败或接口报错时静默处理
    }
  };

  // ============ 渲染 ============

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  if (!profile) {
    return (
      <PageContainer>
        <Text type="warning">获取档案信息失败</Text>
      </PageContainer>
    );
  }

  /** 渲染单个字段值，带编辑按钮或锁定提示 */
  const renderField = (field: string, value: string | undefined) => {
    const isEditable = profile.fieldPermissions?.editableFields?.includes(field);
    const isFlow = profile.fieldPermissions?.flowRequiredFields?.includes(field);

    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <span>{value || '-'}</span>
        {isEditable && (
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(field)}
          >
            编辑
          </Button>
        )}
        {isFlow && !isEditable && (
          <Tooltip title="如需修改请联系 HR">
            <InfoCircleOutlined style={{ color: '#faad14', cursor: 'help' }} />
          </Tooltip>
        )}
      </div>
    );
  };

  return (
    <PageContainer>
      {/* 基础信息 */}
      <Card title="基础信息" bordered={false} style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="姓名">{profile.employeeName}</Descriptions.Item>
          <Descriptions.Item label="性别">
            {profile.gender === 1 ? '男' : profile.gender === 2 ? '女' : profile.genderDesc || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="出生日期">{profile.birthday || '-'}</Descriptions.Item>
          <Descriptions.Item label="工号">{profile.employeeNo}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 个人信息 */}
      <Card title="个人信息" bordered={false} style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="手机号">
            {renderField('phone', profile.phone)}
          </Descriptions.Item>
          <Descriptions.Item label="邮箱">
            {renderField('email', profile.email)}
          </Descriptions.Item>
          <Descriptions.Item label="身份证号">
            <span>{profile.idCard || '-'}</span>
            {profile.fieldPermissions?.flowRequiredFields?.includes('idCard') && (
              <Tooltip title="如需修改请联系 HR">
                <InfoCircleOutlined style={{ color: '#faad14', cursor: 'help', marginLeft: 4 }} />
              </Tooltip>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="紧急联系人">
            {renderField('emergencyContact', profile.emergencyContact)}
          </Descriptions.Item>
          <Descriptions.Item label="紧急联系人电话">
            {renderField('emergencyPhone', profile.emergencyPhone)}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 工作信息 */}
      <Card title="工作信息" bordered={false} style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="部门">{profile.deptName}</Descriptions.Item>
          <Descriptions.Item label="职位">{profile.postName}</Descriptions.Item>
          <Descriptions.Item label="入职日期">{profile.hireDate}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 编辑弹窗 */}
      <Modal
        title={`编辑${FIELD_LABEL_MAP[editingField] || editingField}`}
        open={editModalOpen}
        onOk={handleSave}
        onCancel={() => setEditModalOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name={editingField}
            label={FIELD_LABEL_MAP[editingField] || editingField}
            rules={[{ required: true, message: `请输入${FIELD_LABEL_MAP[editingField] || editingField}` }]}
          >
            <Input placeholder={`请输入${FIELD_LABEL_MAP[editingField] || editingField}`} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ProfileArchivePage;

/**
 * 用户管理页面
 * 功能：用户列表、新增、编辑、删除、重置密码
 */

import React, { useState, useRef } from 'react';
import {
  PageContainer,
  ProTable,
  ProColumns,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormDigit,
} from '@ant-design/pro-components';
import {
  Button,
  message,
  Tag,
  Space,
  Form,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  KeyOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import {
  getUserList,
  createUser,
  updateUser,
  resetPassword,
  getRoleList,
} from '@/services/system';
import dayjs from 'dayjs';
import type { UserItem, RoleItem } from '@/types/system';

const { Text } = Typography;

const UserPage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [resetPwdModalVisible, setResetPwdModalVisible] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserItem | null>(null);
  const [resetPwdResult, setResetPwdResult] = useState<string>('');
  const [roleList, setRoleList] = useState<RoleItem[]>([]);
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const actionRef = useRef<any>();

  // 获取角色列表（用于下拉选择）
  const fetchRoleList = async () => {
    try {
      const res = await getRoleList();
      setRoleList(res || []);
    } catch (error) {
      console.error('获取角色列表失败:', error);
    }
  };

  // 状态标签
  const statusTag = (status: number) => {
    const map: Record<number, { text: string; color: string }> = {
      0: { text: '禁用', color: 'red' },
      1: { text: '启用', color: 'green' },
    };
    const item = map[status] || { text: '未知', color: 'default' };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  // 表格列定义
  const columns: ProColumns<UserItem>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      valueType: 'text',
      fieldProps: {
        placeholder: '请输入用户名/姓名/手机号',
      },
    },
    {
      title: '用户名',
      dataIndex: 'username',
      width: 100,
      fixed: 'left',
      search: false,
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      width: 100,
      search: false,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 120,
      search: false,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 160,
      search: false,
      ellipsis: true,
    },
    {
      title: '角色',
      dataIndex: 'roleNames',
      width: 160,
      search: false,
      render: (_, record) => {
        const roles = record.roleNames || [];
        return (
          <Space size={2} wrap>
            {roles.map((role, index) => (
              <Tag key={index} color="blue" style={{ fontSize: 12, padding: '0 4px', margin: 0 }}>
                {role}
              </Tag>
            ))}
          </Space>
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 70,
      valueEnum: {
        0: { text: '禁用', status: 'Error' },
        1: { text: '启用', status: 'Success' },
      },
      search: false,
      render: (_, record) => statusTag(record.status),
    },
    {
      title: '最后登录',
      dataIndex: 'lastLoginTime',
      width: 160,
      search: false,
      render: (text) => {
        if (!text) return '-';

        // 处理数组格式的时间 [year, month, day, hour, minute, second]
        if (Array.isArray(text)) {
          const [year, month, day, hour = 0, minute = 0, second = 0] = text;
          if (!year || !month || !day) return '-';
          const date = dayjs(new Date(year, month - 1, day, hour, minute, second));
          return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
        }

        // 处理字符串格式的时间
        const date = dayjs(text as string);
        return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
      },
    },
    // {
    //   title: '创建时间',
    //   dataIndex: 'createTime',
    //   width: 140,
    //   search: false,
    // },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 200,
      search: false,
      render: (_, record) => (
        <Space size={0}>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<KeyOutlined />}
            onClick={() => handleResetPwd(record)}
          >
            重置
          </Button>
          {/* 启用/禁用按钮 */}
          {record.status === 1 ? (
            <Button
              type="link"
              size="small"
              danger
              onClick={() => handleToggleStatus(record)}
            >
              禁用
            </Button>
          ) : (
            <Button
              type="link"
              size="small"
              onClick={() => handleToggleStatus(record)}
            >
              启用
            </Button>
          )}
        </Space>
      ),
    },
  ];

  // 查看详情
  const handleViewDetail = (record: UserItem) => {
    setCurrentUser(record);
    setDetailModalVisible(true);
  };

  // 编辑用户
  const handleEdit = async (record: UserItem) => {
    setCurrentUser(record);
    // 先加载角色列表，确保下拉框有数据
    await fetchRoleList();
    editForm.setFieldsValue({
      ...record,
      roleIds: record.roleIds || [],
    });
    setEditModalVisible(true);
  };

  // 切换用户状态（启用/禁用）
  const handleToggleStatus = async (record: UserItem) => {
    const newStatus = record.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '启用' : '禁用';
    try {
      await updateUser(record.id, { status: newStatus });
      message.success(`${actionText}成功`);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || `${actionText}失败`);
    }
  };

  // 重置密码
  const handleResetPwd = (record: UserItem) => {
    setCurrentUser(record);
    setResetPwdResult('');
    setResetPwdModalVisible(true);
  };

  // 确认重置密码
  const confirmResetPwd = async () => {
    if (!currentUser) return;
    try {
      const res = await resetPassword(currentUser.id);
      setResetPwdResult(res.newPassword || '重置成功');
      message.success('密码重置成功');
    } catch (error: any) {
      message.error(error.message || '重置密码失败');
    }
  };

  // 创建用户
  const handleCreate = async (values: any) => {
    try {
      await createUser(values);
      message.success('创建成功');
      setCreateModalVisible(false);
      createForm.resetFields();
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      message.error(error.message || '创建失败');
      return false;
    }
  };

  // 更新用户
  const handleUpdate = async (values: any) => {
    if (!currentUser) return false;
    try {
      await updateUser(currentUser.id, values);
      message.success('更新成功');
      setEditModalVisible(false);
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      message.error(error.message || '更新失败');
      return false;
    }
  };

  return (
    <PageContainer title="用户管理">
      <ProTable<UserItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        search={{
          labelWidth: 'auto',
          searchText: '搜索',
          resetText: '重置',
        }}
        cardBordered
        request={async (params) => {
          const { current: pageNum, pageSize, keyword, status } = params;
          const res = await getUserList({
            pageNum: pageNum || 1,
            pageSize: pageSize || 10,
            keyword,
            status,
          });
          return {
            data: res.records || [],
            success: true,
            total: res.total || 0,
          };
        }}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              fetchRoleList();
              setCreateModalVisible(true);
            }}
          >
            新增用户
          </Button>,
        ]}
      />

      {/* 新增用户弹窗 */}
      <ModalForm
        title="新增用户"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        form={createForm}
        onFinish={handleCreate}
        width={600}
      >
        <ProFormText
          name="username"
          label="用户名"
          rules={[{ required: true, message: '请输入用户名' }]}
          placeholder="请输入用户名"
        />
        <ProFormText.Password
          name="password"
          label="密码"
          rules={[{ required: true, message: '请输入密码' }]}
          placeholder="请输入初始密码"
        />
        <ProFormText
          name="realName"
          label="真实姓名"
          rules={[{ required: true, message: '请输入真实姓名' }]}
          placeholder="请输入真实姓名"
        />
        <ProFormText
          name="phone"
          label="手机号"
          rules={[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
          ]}
          placeholder="请输入手机号"
        />
        <ProFormText
          name="email"
          label="邮箱"
          rules={[{ type: 'email', message: '邮箱格式不正确' }]}
          placeholder="请输入邮箱"
        />
        <ProFormSelect
          name="roleIds"
          label="角色"
          mode="multiple"
          options={roleList.map((role) => ({
            label: role.roleName,
            value: role.id,
          }))}
          placeholder="请选择角色"
        />
        <ProFormSelect
          name="status"
          label="状态"
          initialValue={1}
          options={[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
      </ModalForm>

      {/* 编辑用户弹窗 */}
      <ModalForm
        title="编辑用户"
        open={editModalVisible}
        onOpenChange={setEditModalVisible}
        form={editForm}
        onFinish={handleUpdate}
        width={600}
      >
        <ProFormText
          name="realName"
          label="真实姓名"
          rules={[{ required: true, message: '请输入真实姓名' }]}
        />
        <ProFormText
          name="phone"
          label="手机号"
          rules={[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
          ]}
        />
        <ProFormText
          name="email"
          label="邮箱"
          rules={[{ type: 'email', message: '邮箱格式不正确' }]}
        />
        <ProFormSelect
          name="roleIds"
          label="角色"
          mode="multiple"
          options={roleList.map((role) => ({
            label: role.roleName,
            value: role.id,
          }))}
          placeholder="请选择角色"
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
      </ModalForm>

      {/* 用户详情弹窗 */}
      <ModalForm
        title="用户详情"
        open={detailModalVisible}
        onOpenChange={setDetailModalVisible}
        submitter={false}
        width={600}
      >
        {currentUser && (
          <div style={{ padding: '16px 0' }}>
            <p>
              <strong>用户名：</strong>
              {currentUser.username}
            </p>
            <p>
              <strong>真实姓名：</strong>
              {currentUser.realName || '-'}
            </p>
            <p>
              <strong>手机号：</strong>
              {currentUser.phone || '-'}
            </p>
            <p>
              <strong>邮箱：</strong>
              {currentUser.email || '-'}
            </p>
            <p>
              <strong>角色：</strong>
              {currentUser.roleNames?.join(', ') || '-'}
            </p>
            <p>
              <strong>状态：</strong>
              {statusTag(currentUser.status)}
            </p>
            <p>
              <strong>最后登录时间：</strong>
              {(() => {
                if (!currentUser.lastLoginTime) return '-';
                const text = currentUser.lastLoginTime;

                // 处理数组格式
                if (Array.isArray(text)) {
                  const [year, month, day, hour = 0, minute = 0, second = 0] = text;
                  if (!year || !month || !day) return '-';
                  const date = dayjs(new Date(year, month - 1, day, hour, minute, second));
                  return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
                }

                // 处理字符串格式
                const date = dayjs(text);
                return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
              })()}
            </p>
            <p>
              <strong>创建时间：</strong>
              {(() => {
                if (!currentUser.createTime) return '-';
                const text = currentUser.createTime;

                // 处理数组格式
                if (Array.isArray(text)) {
                  const [year, month, day, hour = 0, minute = 0, second = 0] = text;
                  if (!year || !month || !day) return '-';
                  const date = dayjs(new Date(year, month - 1, day, hour, minute, second));
                  return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
                }

                // 处理字符串格式
                const date = dayjs(text);
                return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '-';
              })()}
            </p>
          </div>
        )}
      </ModalForm>

      {/* 重置密码弹窗 */}
      <ModalForm
        title="重置密码"
        open={resetPwdModalVisible}
        onOpenChange={setResetPwdModalVisible}
        onFinish={async () => {
          await confirmResetPwd();
          return false;
        }}
        submitter={{
          searchConfig: {
            submitText: '确认重置',
            resetText: '关闭',
          },
        }}
        width={500}
      >
        <p>
          确定要重置用户 <strong>{currentUser?.username}</strong> 的密码吗？
        </p>
        {resetPwdResult && (
          <div
            style={{
              marginTop: 16,
              padding: 12,
              backgroundColor: '#f6ffed',
              border: '1px solid #b7eb8f',
              borderRadius: 4,
            }}
          >
            <p style={{ margin: 0 }}>
              <strong>新密码：</strong>
              <Text copyable style={{ fontSize: 16, color: '#52c41a' }}>
                {resetPwdResult}
              </Text>
            </p>
            <p style={{ margin: '8px 0 0', fontSize: 12, color: '#999' }}>
              请妥善保存新密码，重置后无法恢复旧密码。
            </p>
          </div>
        )}
      </ModalForm>
    </PageContainer>
  );
};

export default UserPage;

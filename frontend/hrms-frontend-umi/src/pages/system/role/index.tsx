/**
 * 角色管理页面
 * 功能：角色列表、新增、编辑、删除、分配菜单权限
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
  Tree,
  Divider,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import {
  getRoleList,
  createRole,
  updateRole,
  assignRoleMenus,
  getMenuTree,
} from '@/services/system';
import type { RoleItem, MenuItem } from '@/types/system';
import dayjs from 'dayjs';

const RolePage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [assignMenuModalVisible, setAssignMenuModalVisible] = useState(false);
  const [currentRole, setCurrentRole] = useState<RoleItem | null>(null);
  const [menuList, setMenuList] = useState<MenuItem[]>([]);
  const [menuTreeData, setMenuTreeData] = useState<any[]>([]);
  const [selectedMenuIds, setSelectedMenuIds] = useState<number[]>([]);
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const actionRef = useRef<any>();

  // 格式化时间
  const formatDateTime = (value?: string) => {
    if (!value) return '-';
    const date = dayjs(value);
    if (!date.isValid()) return value;
    return date.format('YYYY-MM-DD HH:mm');
  };

  // 数据权限范围标签
  const dataScopeTag = (scope: number) => {
    const map: Record<number, { text: string; color: string }> = {
      1: { text: '仅本人', color: 'default' },
      2: { text: '本部门', color: 'blue' },
      3: { text: '本部门及子部门', color: 'cyan' },
      4: { text: '全部', color: 'green' },
    };
    const item = map[scope] || { text: '未知', color: 'default' };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  // 表格列定义
  const columns: ProColumns<RoleItem>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      valueType: 'text',
      fieldProps: {
        placeholder: '请输入角色名称/编码',
      },
    },
    {
      title: '角色名称',
      dataIndex: 'roleName',
      width: 120,
      fixed: 'left',
      search: false,
    },
    {
      title: '角色编码',
      dataIndex: 'roleCode',
      width: 100,
      search: false,
    },
    {
      title: '数据权限',
      dataIndex: 'dataScope',
      width: 120,
      search: false,
      render: (_, record) => dataScopeTag(record.dataScope),
    },
    {
      title: '排序',
      dataIndex: 'sortNo',
      width: 60,
      search: false,
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
      render: (_, record) => {
        const map: Record<number, { text: string; color: string }> = {
          0: { text: '禁用', color: 'red' },
          1: { text: '启用', color: 'green' },
        };
        const item = map[record.status] || { text: '未知', color: 'default' };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    // {
    //   title: '创建时间',
    //   dataIndex: 'createTime',
    //   width: 140,
    //   search: false,
    //   render: (value) => formatDateTime(value),
    // },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 200,
      search: false,
      render: (_, record) => (
        <Space size={2} wrap>
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => handleAssignMenu(record)}
            style={{ padding: '0 2px' }}
          >
            权限
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            style={{ padding: '0 2px' }}
          >
            编辑
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

  // 获取菜单列表（树形结构）
  const fetchMenuList = async () => {
    try {
      // 调用树形接口，后端已构建好树形结构
      const res = await getMenuTree();
      // 将后端返回的树形结构转换为 Ant Design Tree 需要的格式
      const treeData = convertToTreeData(res || []);
      setMenuList(res || []);
      setMenuTreeData(treeData);
    } catch (error) {
      console.error('获取菜单列表失败:', error);
    }
  };

  // 将后端菜单树转换为 Ant Design Tree 格式
  const convertToTreeData = (menus: MenuItem[]): any[] => {
    if (!menus || menus.length === 0) return [];

    return menus.map(menu => ({
      title: menu.menuName,
      key: menu.id,
      children: menu.children ? convertToTreeData(menu.children) : [],
    }));
  };

  // 分配菜单权限
  const handleAssignMenu = async (record: RoleItem) => {
    setCurrentRole(record);
    await fetchMenuList();
    setSelectedMenuIds(record.menuIds || []);
    setAssignMenuModalVisible(true);
  };

  // 确认分配菜单
  const confirmAssignMenus = async () => {
    if (!currentRole) return;
    try {
      await assignRoleMenus(currentRole.id, selectedMenuIds);
      message.success('权限分配成功');
      setAssignMenuModalVisible(false);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '权限分配失败');
    }
  };

  // 编辑角色
  const handleEdit = (record: RoleItem) => {
    setCurrentRole(record);
    editForm.setFieldsValue(record);
    setEditModalVisible(true);
  };

  // 切换角色状态（启用/禁用）
  const handleToggleStatus = async (record: RoleItem) => {
    const newStatus = record.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '启用' : '禁用';
    try {
      await updateRole(record.id, { status: newStatus });
      message.success(`${actionText}成功`);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || `${actionText}失败`);
    }
  };

  // 创建角色
  const handleCreate = async (values: any) => {
    try {
      await createRole(values);
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

  // 更新角色
  const handleUpdate = async (values: any) => {
    if (!currentRole) return false;
    try {
      await updateRole(currentRole.id, values);
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
    <PageContainer title="角色管理">
      <ProTable<RoleItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        search={{
          labelWidth: 'auto',
        }}
        cardBordered
        request={async (params) => {
          const { keyword, status } = params;
          const res = await getRoleList({ keyword, status });
          return {
            data: res || [],
            success: true,
            total: res?.length || 0,
          };
        }}
        pagination={false}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setCreateModalVisible(true)}
          >
            新增角色
          </Button>,
        ]}
      />

      {/* 新增角色弹窗 */}
      <ModalForm
        title="新增角色"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        form={createForm}
        onFinish={handleCreate}
        width={600}
      >
        <ProFormText
          name="roleName"
          label="角色名称"
          rules={[{ required: true, message: '请输入角色名称' }]}
          placeholder="请输入角色名称"
        />
        <ProFormText
          name="roleCode"
          label="角色编码"
          rules={[{ required: true, message: '请输入角色编码' }]}
          placeholder="如：ADMIN、HR、MANAGER"
        />
        <ProFormSelect
          name="dataScope"
          label="数据权限范围"
          initialValue={1}
          options={[
            { label: '仅本人', value: 1 },
            { label: '本部门', value: 2 },
            { label: '本部门及子部门', value: 3 },
            { label: '全部', value: 4 },
          ]}
          rules={[{ required: true, message: '请选择数据权限范围' }]}
        />
        <ProFormDigit
          name="sortNo"
          label="排序号"
          initialValue={0}
          min={0}
          fieldProps={{ precision: 0 }}
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

      {/* 编辑角色弹窗 */}
      <ModalForm
        title="编辑角色"
        open={editModalVisible}
        onOpenChange={setEditModalVisible}
        form={editForm}
        onFinish={handleUpdate}
        width={600}
      >
        <ProFormText
          name="roleName"
          label="角色名称"
          rules={[{ required: true, message: '请输入角色名称' }]}
        />
        <ProFormSelect
          name="dataScope"
          label="数据权限范围"
          options={[
            { label: '仅本人', value: 1 },
            { label: '本部门', value: 2 },
            { label: '本部门及子部门', value: 3 },
            { label: '全部', value: 4 },
          ]}
          rules={[{ required: true, message: '请选择数据权限范围' }]}
        />
        <ProFormDigit
          name="sortNo"
          label="排序号"
          min={0}
          fieldProps={{ precision: 0 }}
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

      {/* 分配菜单权限弹窗 */}
      <ModalForm
        title={`分配权限 - ${currentRole?.roleName || ''}`}
        open={assignMenuModalVisible}
        onOpenChange={setAssignMenuModalVisible}
        onFinish={async () => {
          await confirmAssignMenus();
          return false;
        }}
        submitter={{
          searchConfig: {
            submitText: '保存',
            resetText: '取消',
          },
        }}
        width={600}
      >
        <Divider orientation="left">菜单权限</Divider>
        <Tree
          checkable
          treeData={menuTreeData}
          checkedKeys={selectedMenuIds}
          onCheck={(checkedKeys) => {
            setSelectedMenuIds(checkedKeys as number[]);
          }}
          defaultExpandAll
        />
      </ModalForm>
    </PageContainer>
  );
};

export default RolePage;

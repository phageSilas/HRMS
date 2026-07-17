/**
 * 菜单管理页面
 * 功能：菜单树形列表、新增、编辑、删除
 */

import React, { useState, useRef, useEffect } from 'react';
import {
  PageContainer,
  ProTable,
  ProColumns,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormDigit,
  ProFormTextArea,
} from '@ant-design/pro-components';
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Form,
  Tree,
  Card,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FolderOutlined,
  FileOutlined,
  BarsOutlined,
} from '@ant-design/icons';
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/services/system';
import type { MenuItem } from '@/types/system';

const MenuPage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [currentMenu, setCurrentMenu] = useState<MenuItem | null>(null);
  const [menuList, setMenuList] = useState<MenuItem[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const actionRef = useRef<any>();

  // 获取菜单列表
  const fetchMenuList = async () => {
    try {
      const res = await getMenuList();
      const list = res || [];
      setMenuList(list);
      // 默认展开所有节点
      setExpandedKeys(list.map((item) => item.id));
    } catch (error) {
      console.error('获取菜单列表失败:', error);
    }
  };

  useEffect(() => {
    fetchMenuList();
  }, []);

  // 菜单类型标签
  const menuTypeTag = (type: number) => {
    const map: Record<number, { text: string; color: string; icon: React.ReactNode }> = {
      0: { text: '目录', color: 'blue', icon: <FolderOutlined /> },
      1: { text: '菜单', color: 'green', icon: <FileOutlined /> },
      2: { text: '按钮', color: 'orange', icon: <BarsOutlined /> },
    };
    const item = map[type] || { text: '未知', color: 'default', icon: null };
    return (
      <Tag color={item.color} icon={item.icon}>
        {item.text}
      </Tag>
    );
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
  const columns: ProColumns<MenuItem>[] = [
    {
      title: '菜单名称',
      dataIndex: 'menuName',
      width: 180,
      fixed: 'left',
      search: false,
    },
    {
      title: '类型',
      dataIndex: 'menuType',
      width: 100,
      search: false,
      render: (_, record) => menuTypeTag(record.menuType),
    },
    {
      title: '路径',
      dataIndex: 'path',
      width: 200,
      search: false,
      ellipsis: true,
    },
    {
      title: '组件',
      dataIndex: 'component',
      width: 200,
      search: false,
      ellipsis: true,
    },
    {
      title: '权限标识',
      dataIndex: 'permission',
      width: 180,
      search: false,
    },
    {
      title: '图标',
      dataIndex: 'icon',
      width: 100,
      search: false,
    },
    {
      title: '排序',
      dataIndex: 'sortNo',
      width: 80,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      search: false,
      render: (_, record) => statusTag(record.status),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 260,
      search: false,
      render: (_, record) => (
        <Space size={4} wrap>
          <Button
            type="link"
            size="small"
            icon={<PlusOutlined />}
            onClick={() => handleAddChild(record)}
          >
            新增子项
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description={`确定要删除菜单 "${record.menuName}" 吗？删除后其下所有子菜单也将被删除。`}
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger size="small" icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 新增子菜单
  const handleAddChild = (record: MenuItem) => {
    setCurrentMenu(record);
    createForm.setFieldsValue({
      parentId: record.id,
      menuType: 1,
      status: 1,
      sortNo: 0,
    });
    setCreateModalVisible(true);
  };

  // 编辑菜单
  const handleEdit = (record: MenuItem) => {
    setCurrentMenu(record);
    editForm.setFieldsValue(record);
    setEditModalVisible(true);
  };

  // 删除菜单
  const handleDelete = async (id: number) => {
    try {
      await deleteMenu(id);
      message.success('删除成功');
      fetchMenuList();
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 创建菜单
  const handleCreate = async (values: any) => {
    try {
      await createMenu(values);
      message.success('创建成功');
      setCreateModalVisible(false);
      createForm.resetFields();
      fetchMenuList();
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      message.error(error.message || '创建失败');
      return false;
    }
  };

  // 更新菜单
  const handleUpdate = async (values: any) => {
    if (!currentMenu) return false;
    try {
      await updateMenu(currentMenu.id, values);
      message.success('更新成功');
      setEditModalVisible(false);
      fetchMenuList();
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      message.error(error.message || '更新失败');
      return false;
    }
  };

  // 构建树形数据
  const buildTreeData = (menus: MenuItem[]): any[] => {
    const menuMap = new Map<number, MenuItem>();
    menus.forEach((menu) => menuMap.set(menu.id, menu));

    const treeData: any[] = [];
    menus.forEach((menu) => {
      const node = {
        title: menu.menuName,
        key: menu.id,
        children: [] as any[],
      };
      if (menu.parentId === 0 || !menu.parentId) {
        treeData.push(node);
      } else {
        const parent = menuMap.get(menu.parentId);
        if (parent) {
          const parentNode = findNodeInTree(treeData, menu.parentId);
          if (parentNode) {
            parentNode.children = parentNode.children || [];
            parentNode.children.push(node);
          }
        }
      }
    });
    return treeData;
  };

  const findNodeInTree = (tree: any[], key: number): any | null => {
    for (const node of tree) {
      if (node.key === key) return node;
      if (node.children) {
        const found = findNodeInTree(node.children, key);
        if (found) return found;
      }
    }
    return null;
  };

  // 获取父菜单选项
  const getParentOptions = () => {
    const options = [{ label: '顶级菜单', value: 0 }];
    menuList
      .filter((menu) => menu.menuType === 0)
      .forEach((menu) => {
        options.push({
          label: menu.menuName,
          value: menu.id,
        });
      });
    return options;
  };

  return (
    <PageContainer title="菜单管理">
      <Row gutter={16}>
        {/* 左侧菜单树 */}
        <Col span={6}>
          <Card title="菜单结构" size="small">
            <Tree
              treeData={buildTreeData(menuList)}
              expandedKeys={expandedKeys}
              onExpand={(keys) => setExpandedKeys(keys)}
              defaultExpandAll
              showLine
              showIcon
            />
          </Card>
        </Col>

        {/* 右侧菜单列表 */}
        <Col span={18}>
          <ProTable<MenuItem>
            actionRef={actionRef}
            columns={columns}
            rowKey="id"
            search={{
              labelWidth: 'auto',
            }}
            cardBordered
            request={async (params) => {
              const { keyword, status } = params;
              const res = await getMenuList({ keyword, status });
              const list = res || [];
              setMenuList(list);
              return {
                data: list,
                success: true,
                total: list.length,
              };
            }}
            pagination={false}
            toolBarRender={() => [
              <Button
                key="add"
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  createForm.setFieldsValue({
                    parentId: 0,
                    menuType: 0,
                    status: 1,
                    sortNo: 0,
                  });
                  setCreateModalVisible(true);
                }}
              >
                新增菜单
              </Button>,
            ]}
          />
        </Col>
      </Row>

      {/* 新增菜单弹窗 */}
      <ModalForm
        title="新增菜单"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        form={createForm}
        onFinish={handleCreate}
        width={600}
      >
        <ProFormSelect
          name="parentId"
          label="上级菜单"
          options={getParentOptions()}
          initialValue={0}
          rules={[{ required: true, message: '请选择上级菜单' }]}
        />
        <ProFormText
          name="menuName"
          label="菜单名称"
          rules={[{ required: true, message: '请输入菜单名称' }]}
          placeholder="请输入菜单名称"
        />
        <ProFormSelect
          name="menuType"
          label="菜单类型"
          initialValue={0}
          options={[
            { label: '目录', value: 0 },
            { label: '菜单', value: 1 },
            { label: '按钮', value: 2 },
          ]}
          rules={[{ required: true, message: '请选择菜单类型' }]}
        />
        <ProFormText
          name="path"
          label="路由路径"
          placeholder="如：/system/user"
        />
        <ProFormText
          name="component"
          label="组件路径"
          placeholder="如：@/pages/system/user"
        />
        <ProFormText
          name="permission"
          label="权限标识"
          placeholder="如：system:user:list"
        />
        <ProFormText
          name="icon"
          label="图标"
          placeholder="如：UserOutlined"
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

      {/* 编辑菜单弹窗 */}
      <ModalForm
        title="编辑菜单"
        open={editModalVisible}
        onOpenChange={setEditModalVisible}
        form={editForm}
        onFinish={handleUpdate}
        width={600}
      >
        <ProFormSelect
          name="parentId"
          label="上级菜单"
          options={getParentOptions()}
          rules={[{ required: true, message: '请选择上级菜单' }]}
        />
        <ProFormText
          name="menuName"
          label="菜单名称"
          rules={[{ required: true, message: '请输入菜单名称' }]}
        />
        <ProFormSelect
          name="menuType"
          label="菜单类型"
          options={[
            { label: '目录', value: 0 },
            { label: '菜单', value: 1 },
            { label: '按钮', value: 2 },
          ]}
          rules={[{ required: true, message: '请选择菜单类型' }]}
        />
        <ProFormText name="path" label="路由路径" />
        <ProFormText name="component" label="组件路径" />
        <ProFormText name="permission" label="权限标识" />
        <ProFormText name="icon" label="图标" />
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
    </PageContainer>
  );
};

export default MenuPage;

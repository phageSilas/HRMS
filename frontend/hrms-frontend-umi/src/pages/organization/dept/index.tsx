/**
 * 部门管理页面
 * 功能：左侧部门树，右侧显示选中部门的详细信息
 */

import React, { useState, useRef, useEffect } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Row,
  Tree,
  Descriptions,
  Tag,
  Space,
  Empty,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  ApartmentOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  getDeptTree,
  getDeptDetail,
  createDept,
  updateDept,
  deleteDept,
} from '@/services/organization';
import { hasEmployeesInDept } from '@/services/employee';
import type { DeptTreeNode, DeptDetail } from '@/services/organization';

const { TextArea } = Input;

const DeptPage: React.FC = () => {
  const [treeData, setTreeData] = useState<DeptTreeNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新增部门');
  const [currentDept, setCurrentDept] = useState<DeptDetail | null>(null);
  const [selectedDeptId, setSelectedDeptId] = useState<number | null>(null);
  const [selectedDeptName, setSelectedDeptName] = useState<string>('');
  const [form] = Form.useForm();
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  // 加载部门树
  const fetchDeptTree = async () => {
    setLoading(true);
    try {
      const data = await getDeptTree();
      setTreeData(data || []);
      // 默认展开第一级
      if (data && data.length > 0) {
        setExpandedKeys(data.map(node => node.id));
      }
    } catch (error: any) {
      message.error(error.message || '获取部门树失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeptTree();
  }, []);

  // 选中部门节点，加载详情
  const handleSelectDept = async (deptId: number, deptName: string) => {
    setSelectedDeptId(deptId);
    setSelectedDeptName(deptName);
    setDetailLoading(true);
    try {
      const detail = await getDeptDetail(deptId);
      setCurrentDept(detail);
    } catch (error: any) {
      message.error(error.message || '获取部门详情失败');
      setCurrentDept(null);
    } finally {
      setDetailLoading(false);
    }
  };

  // 打开新增弹窗
  const handleAdd = (parentId?: number, parentName?: string) => {
    setModalTitle('新增部门');
    setCurrentDept(null);
    form.resetFields();
    if (parentId) {
      form.setFieldsValue({ parentId, parentName });
    } else {
      form.setFieldsValue({ parentId: 0, parentName: '根部门' });
    }
    setModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = async () => {
    if (!selectedDeptId) {
      message.warning('请先选择一个部门');
      return;
    }
    try {
      const detail = await getDeptDetail(selectedDeptId);
      setCurrentDept(detail);
      setModalTitle('编辑部门');
      form.setFieldsValue({
        deptName: detail.deptName,
        leaderUserId: detail.leaderUserId,
        sortNo: detail.sortNo,
        remark: detail.remark,
      });
      setModalVisible(true);
    } catch (error: any) {
      message.error(error.message || '获取部门详情失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (currentDept) {
        // 编辑
        await updateDept(currentDept.id, values);
        message.success('更新成功');
        // 刷新详情
        handleSelectDept(currentDept.id, values.deptName);
      } else {
        // 新增
        await createDept(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      fetchDeptTree();
    } catch (error: any) {
      message.error(error.message || '操作失败');
    }
  };

  // 删除部门
  const handleDelete = async (id: number) => {
    try {
      // 1. 检查部门下是否有在职员工
      const hasEmployees = await hasEmployeesInDept(id);
      if (hasEmployees) {
        message.error('该部门下有在职员工，无法删除');
        return;
      }

      // 2. 调用删除接口（后端会检查子部门）
      await deleteDept(id);
      message.success('删除成功');
      // 清空选中状态
      setSelectedDeptId(null);
      setSelectedDeptName('');
      setCurrentDept(null);
      fetchDeptTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 渲染树节点标题
  const renderTreeTitle = (node: DeptTreeNode) => (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        width: '100%',
        paddingRight: 8,
      }}
    >
      <span>
        <ApartmentOutlined style={{ marginRight: 8, color: '#1890ff' }} />
        {node.deptName}
      </span>
      <span style={{ marginLeft: 8, color: '#999', fontSize: 12 }}>
        ({node.employeeCount || 0}人)
      </span>
    </div>
  );

  // 将树数据转换为 Tree 组件需要的格式
  const convertTreeData = (nodes: DeptTreeNode[]): any[] => {
    return nodes.map((node) => ({
      key: node.id,
      title: renderTreeTitle(node),
      children: node.children ? convertTreeData(node.children) : undefined,
    }));
  };

  return (
    <PageContainer title="部门管理">
      <Row gutter={16} style={{ minHeight: 'calc(100vh - 200px)' }}>
        {/* 左侧部门树 */}
        <Col span={6}>
          <Card
            title={
              <span>
                <TeamOutlined style={{ marginRight: 8 }} />
                部门架构
              </span>
            }
            extra={
              <Button
                type="link"
                icon={<PlusOutlined />}
                onClick={() => handleAdd()}
                style={{ padding: 0 }}
              >
                新增
              </Button>
            }
            loading={loading}
            bodyStyle={{ padding: '12px 0' }}
          >
            {treeData.length > 0 ? (
              <Tree
                treeData={convertTreeData(treeData)}
                expandedKeys={expandedKeys}
                onExpand={(keys) => setExpandedKeys(keys)}
                selectedKeys={selectedDeptId ? [selectedDeptId] : []}
                onSelect={(selectedKeys, info: any) => {
                  if (selectedKeys.length > 0) {
                    const node = info.node;
                    const deptId = selectedKeys[0] as number;
                    // 从 treeData 中找到对应的节点名称
                    const findNodeName = (nodes: DeptTreeNode[]): string => {
                      for (const n of nodes) {
                        if (n.id === deptId) return n.deptName;
                        if (n.children) {
                          const name = findNodeName(n.children);
                          if (name) return name;
                        }
                      }
                      return '';
                    };
                    const deptName = findNodeName(treeData);
                    handleSelectDept(deptId, deptName);
                  }
                }}
                showLine={{ showLeafIcon: false }}
                style={{ fontSize: 14 }}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                <ApartmentOutlined style={{ fontSize: 48, marginBottom: 16, color: '#d9d9d9' }} />
                <p>暂无部门数据</p>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
                  新增根部门
                </Button>
              </div>
            )}
          </Card>
        </Col>

        {/* 右侧部门详情 */}
        <Col span={18}>
          <Card
            title={
              <span>
                <ApartmentOutlined style={{ marginRight: 8 }} />
                {selectedDeptName || '部门详情'}
              </span>
            }
            extra={
              selectedDeptId && (
                <Space>
                  <Button
                    type="primary"
                    icon={<EditOutlined />}
                    onClick={handleEdit}
                  >
                    编辑
                  </Button>
                  <Button
                    icon={<PlusOutlined />}
                    onClick={() => handleAdd(selectedDeptId, selectedDeptName)}
                  >
                    新增子部门
                  </Button>
                  <Popconfirm
                    title="确认删除"
                    description={`确定要删除部门 "${selectedDeptName}" 吗？`}
                    onConfirm={() => handleDelete(selectedDeptId)}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button danger icon={<DeleteOutlined />}>
                      删除
                    </Button>
                  </Popconfirm>
                </Space>
              )
            }
            loading={detailLoading}
          >
            {currentDept ? (
              <Descriptions
                bordered
                column={2}
                labelStyle={{ width: 150, fontWeight: 500 }}
              >
                <Descriptions.Item label="部门名称" span={2}>
                  <span style={{ fontSize: 16, fontWeight: 500 }}>{currentDept.deptName}</span>
                </Descriptions.Item>
                <Descriptions.Item label="部门编码">
                  {currentDept.deptCode}
                </Descriptions.Item>
                <Descriptions.Item label="上级部门">
                  {currentDept.parentName || '根部门'}
                </Descriptions.Item>
                <Descriptions.Item label="部门负责人">
                  {currentDept.leaderName || (
                    <span style={{ color: '#999' }}>未设置</span>
                  )}
                </Descriptions.Item>
                <Descriptions.Item label="员工数量">
                  <Tag color="blue" icon={<UserOutlined />}>
                    {currentDept.employeeCount || 0} 人
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="部门层级">
                  第 {currentDept.deptLevel} 级
                </Descriptions.Item>
                <Descriptions.Item label="排序号">
                  {currentDept.sortNo}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={currentDept.status === 1 ? 'success' : 'error'}>
                    {currentDept.status === 1 ? '启用' : '禁用'}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="创建时间">
                  {currentDept.createTime || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="备注" span={2}>
                  {currentDept.remark || <span style={{ color: '#999' }}>无</span>}
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="请从左侧选择一个部门查看详情"
                style={{ padding: '80px 0' }}
              />
            )}
          </Card>
        </Col>
      </Row>

      {/* 新增/编辑部门弹窗 */}
      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
        }}
        width={600}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ parentId: 0, sortNo: 1 }}
        >
          {!currentDept && (
            <>
              <Form.Item
                name="deptCode"
                label="部门编码"
                rules={[{ required: true, message: '请输入部门编码' }]}
              >
                <Input placeholder="请输入部门编码，如：RD001" />
              </Form.Item>
              <Form.Item
                name="parentName"
                label="上级部门"
              >
                <Input disabled placeholder="上级部门" />
              </Form.Item>
              <Form.Item name="parentId" hidden>
                <InputNumber />
              </Form.Item>
            </>
          )}
          <Form.Item
            name="deptName"
            label="部门名称"
            rules={[{ required: true, message: '请输入部门名称' }]}
          >
            <Input placeholder="请输入部门名称" />
          </Form.Item>
          <Form.Item
            name="leaderUserId"
            label="部门负责人"
          >
            <InputNumber style={{ width: '100%' }} placeholder="请输入负责人用户ID" />
          </Form.Item>
          <Form.Item
            name="sortNo"
            label="排序号"
            initialValue={1}
          >
            <InputNumber style={{ width: '100%' }} placeholder="请输入排序号" />
          </Form.Item>
          <Form.Item
            name="remark"
            label="备注"
          >
            <TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default DeptPage;
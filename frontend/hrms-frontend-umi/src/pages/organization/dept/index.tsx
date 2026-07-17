/**
 * 部门管理页面
 * 功能：部门树展示、新增、编辑、删除部门
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
  Space,
  Table,
  Tree,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  ApartmentOutlined,
} from '@ant-design/icons';
import {
  getDeptTree,
  getDeptDetail,
  createDept,
  updateDept,
  deleteDept,
} from '@/services/organization';
import type { DeptTreeNode, DeptDetail } from '@/services/organization';

const { TextArea } = Input;

const DeptPage: React.FC = () => {
  const [treeData, setTreeData] = useState<DeptTreeNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新增部门');
  const [currentDept, setCurrentDept] = useState<DeptDetail | null>(null);
  const [selectedDeptId, setSelectedDeptId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  // 加载部门树
  const fetchDeptTree = async () => {
    setLoading(true);
    try {
      const data = await getDeptTree();
      setTreeData(data || []);
    } catch (error: any) {
      message.error(error.message || '获取部门树失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeptTree();
  }, []);

  // 查看部门详情
  const handleViewDetail = async (id: number) => {
    try {
      const detail = await getDeptDetail(id);
      setCurrentDept(detail);
      setDetailVisible(true);
    } catch (error: any) {
      message.error(error.message || '获取部门详情失败');
    }
  };

  // 打开新增弹窗
  const handleAdd = (parentId?: number) => {
    setModalTitle('新增部门');
    setCurrentDept(null);
    form.resetFields();
    if (parentId) {
      form.setFieldsValue({ parentId });
    }
    setModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = async (id: number) => {
    try {
      const detail = await getDeptDetail(id);
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
      await deleteDept(id);
      message.success('删除成功');
      fetchDeptTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 渲染树节点标题
  const renderTreeTitle = (node: DeptTreeNode) => (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
      <span>
        <ApartmentOutlined style={{ marginRight: 8, color: '#1890ff' }} />
        {node.deptName}
        <span style={{ marginLeft: 8, color: '#999', fontSize: 12 }}>
          ({node.employeeCount || 0}人)
        </span>
      </span>
      <span style={{ marginLeft: 16 }}>
        <Button
          type="link"
          size="small"
          icon={<PlusOutlined />}
          onClick={(e) => {
            e.stopPropagation();
            handleAdd(node.id);
          }}
        >
          新增子部门
        </Button>
        <Button
          type="link"
          size="small"
          icon={<EditOutlined />}
          onClick={(e) => {
            e.stopPropagation();
            handleEdit(node.id);
          }}
        >
          编辑
        </Button>
        <Popconfirm
          title="确认删除"
          description={`确定要删除部门 "${node.deptName}" 吗？`}
          onConfirm={(e) => {
            e?.stopPropagation();
            handleDelete(node.id);
          }}
          okText="确定"
          cancelText="取消"
        >
          <Button
            type="link"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={(e) => e.stopPropagation()}
          >
            删除
          </Button>
        </Popconfirm>
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
      <Row gutter={16}>
        {/* 左侧部门树 */}
        <Col span={8}>
          <Card
            title={
              <span>
                <TeamOutlined style={{ marginRight: 8 }} />
                部门架构
              </span>
            }
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
                新增根部门
              </Button>
            }
            loading={loading}
          >
            {treeData.length > 0 ? (
              <Tree
                treeData={convertTreeData(treeData)}
                expandedKeys={expandedKeys}
                onExpand={(keys) => setExpandedKeys(keys)}
                defaultExpandAll
                selectable={false}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                暂无部门数据
              </div>
            )}
          </Card>
        </Col>

        {/* 右侧部门详情/操作区 */}
        <Col span={16}>
          <Card title="部门信息概览">
            {treeData.length > 0 ? (
              <div>
                <p style={{ marginBottom: 16 }}>
                  <strong>部门总数：</strong>
                  {(() => {
                    let count = 0;
                    const countDepts = (nodes: DeptTreeNode[]) => {
                      nodes.forEach((node) => {
                        count++;
                        if (node.children) {
                          countDepts(node.children);
                        }
                      });
                    };
                    countDepts(treeData);
                    return count;
                  })()}
                  个
                </p>
                <p style={{ marginBottom: 16 }}>
                  <strong>最大层级：</strong>5级（根部门为第1级）
                </p>
                <p style={{ color: '#999', fontSize: 12 }}>
                  提示：点击左侧部门可查看详情，右键或点击操作按钮可进行编辑、删除、新增子部门等操作。
                </p>
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                <ApartmentOutlined style={{ fontSize: 48, marginBottom: 16, color: '#d9d9d9' }} />
                <p>暂无部门数据，请先新增部门</p>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
                  新增根部门
                </Button>
              </div>
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
                name="parentId"
                label="上级部门"
                initialValue={0}
              >
                <InputNumber style={{ width: '100%' }} disabled placeholder="根部门" />
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

      {/* 部门详情弹窗 */}
      <Modal
        title="部门详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={
          <Button onClick={() => setDetailVisible(false)}>关闭</Button>
        }
        width={600}
      >
        {currentDept && (
          <div style={{ padding: '16px 0' }}>
            <p>
              <strong>部门名称：</strong>
              {currentDept.deptName}
            </p>
            <p>
              <strong>部门编码：</strong>
              {currentDept.deptCode}
            </p>
            <p>
              <strong>上级部门：</strong>
              {currentDept.parentName || '根部门'}
            </p>
            <p>
              <strong>部门层级：</strong>
              第{currentDept.deptLevel}级
            </p>
            <p>
              <strong>员工数量：</strong>
              {currentDept.employeeCount || 0}人
            </p>
            <p>
              <strong>排序号：</strong>
              {currentDept.sortNo}
            </p>
            <p>
              <strong>状态：</strong>
              {currentDept.status === 1 ? '启用' : '禁用'}
            </p>
            <p>
              <strong>备注：</strong>
              {currentDept.remark || '-'}
            </p>
            <p>
              <strong>创建时间：</strong>
              {currentDept.createTime}
            </p>
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default DeptPage;

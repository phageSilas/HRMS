/**
 * 职位管理页面
 * 功能：职位列表、新增、编辑、删除
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
} from '@ant-design/pro-components';
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Form,
  Input,
  Select,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import {
  getPostList,
  getPostDetail,
  createPost,
  updatePost,
  deletePost,
  getDeptTree,
  getPostStatsBySequence,
} from '@/services/organization';
import { hasEmployeesInPost } from '@/services/employee';
import type { PostItem, DeptTreeNode } from '@/services/organization';
import { Card, Row, Col, Statistic } from 'antd';
import { TeamOutlined, SolutionOutlined, ToolOutlined } from '@ant-design/icons';

const PostPage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentPost, setCurrentPost] = useState<PostItem | null>(null);
  const [deptList, setDeptList] = useState<{ label: string; value: number }[]>([]);
  const [postStats, setPostStats] = useState<Record<string, number>>({ M: 0, P: 0, S: 0 });
  const [sequenceFilter, setSequenceFilter] = useState<string>(''); // 序列筛选状态
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const actionRef = useRef<any>();

  // 序列选项
  const sequenceOptions = [
    { label: '管理序列', value: 'M' },
    { label: '专业序列', value: 'P' },
    { label: '支持序列', value: 'S' },
  ];

  // 获取部门列表（用于下拉选择）
  const fetchDeptList = async () => {
    try {
      const data = await getDeptTree();
      const flatten = (nodes: DeptTreeNode[], level: number = 0): { label: string; value: number }[] => {
        const result: { label: string; value: number }[] = [];
        nodes.forEach((node) => {
          result.push({
            label: `${'　'.repeat(level)}${node.deptName}`,
            value: node.id,
          });
          if (node.children && node.children.length > 0) {
            result.push(...flatten(node.children, level + 1));
          }
        });
        return result;
      };
      setDeptList(flatten(data || []));
    } catch (error) {
      console.error('获取部门列表失败:', error);
    }
  };

  // 获取职位统计
  const fetchPostStats = async () => {
    try {
      const stats = await getPostStatsBySequence();
      setPostStats(stats);
    } catch (error) {
      console.error('获取职位统计失败:', error);
    }
  };

  useEffect(() => {
    fetchDeptList();
    fetchPostStats();
  }, []);

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
  const columns: ProColumns<PostItem>[] = [
    {
      title: '职位名称',
      dataIndex: 'postName',
      width: 180,
      fixed: 'left',
    },
    {
      title: '职位编码',
      dataIndex: 'postCode',
      width: 150,
    },
    {
      title: '所属序列',
      dataIndex: 'sequenceName',
      width: 120,
      search: false,
      render: (_, record) => (
        <Tag color="blue">{record.sequenceName || record.sequenceCode}</Tag>
      ),
    },
    {
      title: '所属部门',
      dataIndex: 'deptName',
      width: 200,
      search: false,
      render: (text) => text || <span style={{ color: '#999' }}>全公司通用</span>,
    },
    {
      title: '职级范围',
      dataIndex: 'jobLevelRange',
      width: 150,
      search: false,
      render: (_, record) => {
        if (record.jobLevelMin && record.jobLevelMax) {
          return `${record.jobLevelMin} ~ ${record.jobLevelMax}`;
        }
        return '-';
      },
    },
    {
      title: '试用期',
      dataIndex: 'defaultProbationMonth',
      width: 100,
      search: false,
      render: (text) => (text ? `${text}个月` : '-'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      search: false,
      render: (_, record) => statusTag(record.status),
    },
    // {
    //   title: '排序号',
    //   dataIndex: 'sortNo',
    //   width: 100,
    //   search: false,
    // },
    // {
    //   title: '创建时间',
    //   dataIndex: 'createTime',
    //   width: 180,
    //   search: false,
    // },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 180,
      search: false,
      render: (_, record) => (
        <Space size={2} wrap>
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
            description={`确定要删除职位 "${record.postName}" 吗？`}
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

  // 查看详情
  const handleViewDetail = (record: PostItem) => {
    setCurrentPost(record);
    setDetailModalVisible(true);
  };

  // 编辑职位
  const handleEdit = (record: PostItem) => {
    setCurrentPost(record);
    editForm.setFieldsValue({
      postName: record.postName,
      sequenceCode: record.sequenceCode,
      deptId: record.deptId,
      jobLevelMin: record.jobLevelMin,
      jobLevelMax: record.jobLevelMax,
      defaultProbationMonth: record.defaultProbationMonth,
      sortNo: record.sortNo,
    });
    setEditModalVisible(true);
  };

  // 删除职位
  const handleDelete = async (id: number) => {
    try {
      // 检查职位下是否有在职员工
      const hasEmployees = await hasEmployeesInPost(id);
      if (hasEmployees) {
        message.error('该职位下有在职员工，无法删除');
        return;
      }

      await deletePost(id);
      message.success('删除成功');
      actionRef.current?.reload();
      fetchPostStats(); // 刷新统计
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 创建职位
  const handleCreate = async (values: any) => {
    try {
      await createPost(values);
      message.success('创建成功');
      setCreateModalVisible(false);
      createForm.resetFields();
      actionRef.current?.reload();
      fetchPostStats(); // 刷新统计
      return true;
    } catch (error: any) {
      message.error(error.message || '创建失败');
      return false;
    }
  };

  // 更新职位
  const handleUpdate = async (values: any) => {
    if (!currentPost) return false;
    try {
      await updatePost(currentPost.id, values);
      message.success('更新成功');
      setEditModalVisible(false);
      actionRef.current?.reload();
      fetchPostStats(); // 刷新统计
      return true;
    } catch (error: any) {
      message.error(error.message || '更新失败');
      return false;
    }
  };

  return (
    <PageContainer title="职位管理">
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="管理序列"
              value={postStats.M || 0}
              prefix={<TeamOutlined />}
              suffix="个职位"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="专业序列"
              value={postStats.P || 0}
              prefix={<SolutionOutlined />}
              suffix="个职位"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="支持序列"
              value={postStats.S || 0}
              prefix={<ToolOutlined />}
              suffix="个职位"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 职位列表表格 */}
      <ProTable<PostItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        search={{
          labelWidth: 'auto',
          searchText: '搜索',
          resetText: '重置',
          collapsed: true,
          collapseRender: false, // 隐藏展开/收起按钮
          span: {
            xs: 24,
            sm: 12,
            md: 8,
            lg: 6,
          },
        }}
        cardBordered
        request={async (params) => {
          const { current: pageNum, pageSize, postName, postCode } = params;
          const res = await getPostList({
            pageNum: pageNum || 1,
            pageSize: pageSize || 10,
            keyword: postName || postCode,
            sequenceCode: sequenceFilter, // 添加序列筛选
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
        headerTitle={
          // 序列筛选标签（左侧）
          <Space>
            <Button
              type={sequenceFilter === '' ? 'primary' : 'default'}
              onClick={() => {
                setSequenceFilter('');
                actionRef.current?.reload();
              }}
            >
              全部
            </Button>
            <Button
              type={sequenceFilter === 'M' ? 'primary' : 'default'}
              onClick={() => {
                setSequenceFilter('M');
                actionRef.current?.reload();
              }}
            >
              管理序列
            </Button>
            <Button
              type={sequenceFilter === 'P' ? 'primary' : 'default'}
              onClick={() => {
                setSequenceFilter('P');
                actionRef.current?.reload();
              }}
            >
              专业序列
            </Button>
            <Button
              type={sequenceFilter === 'S' ? 'primary' : 'default'}
              onClick={() => {
                setSequenceFilter('S');
                actionRef.current?.reload();
              }}
            >
              支持序列
            </Button>
          </Space>
        }
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              fetchDeptList();
              setCreateModalVisible(true);
            }}
          >
            新增职位
          </Button>,
        ]}
      />

      {/* 新增职位弹窗 */}
      <ModalForm
        title="新增职位"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        form={createForm}
        onFinish={handleCreate}
        width={680}
        modalProps={{
          destroyOnClose: true,
        }}
        layout="horizontal"
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 18 }}
      >
        <ProFormText
          name="postName"
          label="职位名称"
          rules={[{ required: true, message: '请输入职位名称' }]}
          placeholder="请输入职位名称"
        />
        <ProFormText
          name="postCode"
          label="职位编码"
          rules={[{ required: true, message: '请输入职位编码' }]}
          placeholder="请输入职位编码，如：JAVA_DEV"
        />
        <ProFormSelect
          name="sequenceCode"
          label="职位序列"
          rules={[{ required: true, message: '请选择职位序列' }]}
          options={sequenceOptions}
          placeholder="请选择职位序列"
        />
        <ProFormSelect
          name="deptId"
          label="所属部门"
          options={deptList}
          placeholder="请选择所属部门（不选则为全公司通用）"
        />
        <ProFormText
          name="jobLevelMin"
          label="职级下限"
          placeholder="如：P3"
        />
        <ProFormText
          name="jobLevelMax"
          label="职级上限"
          placeholder="如：P7"
        />
        <ProFormDigit
          name="defaultProbationMonth"
          label="默认试用期（月）"
          min={0}
          max={12}
          placeholder="请输入默认试用期"
        />
        <ProFormDigit
          name="sortNo"
          label="排序号"
          initialValue={1}
          placeholder="请输入排序号"
        />
      </ModalForm>

      {/* 编辑职位弹窗 */}
      <ModalForm
        title="编辑职位"
        open={editModalVisible}
        onOpenChange={setEditModalVisible}
        form={editForm}
        onFinish={handleUpdate}
        width={680}
        modalProps={{
          destroyOnClose: true,
        }}
        layout="horizontal"
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 18 }}
      >
        <ProFormText
          name="postName"
          label="职位名称"
          rules={[{ required: true, message: '请输入职位名称' }]}
        />
        <ProFormSelect
          name="sequenceCode"
          label="职位序列"
          rules={[{ required: true, message: '请选择职位序列' }]}
          options={sequenceOptions}
        />
        <ProFormSelect
          name="deptId"
          label="所属部门"
          options={deptList}
        />
        <ProFormText
          name="jobLevelMin"
          label="职级下限"
          placeholder="如：P3"
        />
        <ProFormText
          name="jobLevelMax"
          label="职级上限"
          placeholder="如：P7"
        />
        <ProFormDigit
          name="defaultProbationMonth"
          label="默认试用期（月）"
          min={0}
          max={12}
        />
        <ProFormDigit
          name="sortNo"
          label="排序号"
        />
      </ModalForm>

      {/* 职位详情弹窗 */}
      <ModalForm
        title="职位详情"
        open={detailModalVisible}
        onOpenChange={setDetailModalVisible}
        submitter={false}
        width={600}
      >
        {currentPost && (
          <div style={{ padding: '16px 0' }}>
            <p>
              <strong>职位名称：</strong>
              {currentPost.postName}
            </p>
            <p>
              <strong>职位编码：</strong>
              {currentPost.postCode}
            </p>
            <p>
              <strong>职位序列：</strong>
              {currentPost.sequenceName || currentPost.sequenceCode}
            </p>
            <p>
              <strong>所属部门：</strong>
              {currentPost.deptName || '全公司通用'}
            </p>
            <p>
              <strong>职级范围：</strong>
              {currentPost.jobLevelMin && currentPost.jobLevelMax
                ? `${currentPost.jobLevelMin} ~ ${currentPost.jobLevelMax}`
                : '-'}
            </p>
            <p>
              <strong>默认试用期：</strong>
              {currentPost.defaultProbationMonth ? `${currentPost.defaultProbationMonth}个月` : '-'}
            </p>
            <p>
              <strong>状态：</strong>
              {statusTag(currentPost.status)}
            </p>
            <p>
              <strong>排序号：</strong>
              {currentPost.sortNo}
            </p>
            <p>
              <strong>创建时间：</strong>
              {currentPost.createTime}
            </p>
          </div>
        )}
      </ModalForm>
    </PageContainer>
  );
};

export default PostPage;

/**
 * 员工列表页面
 * 负责人：成员 B
 *
 * 功能：高级搜索、分页表格、状态标签、行内操作（查看/编辑/更多）
 */

import { getDepartmentTree } from '@/services/organization';
import { getPostList, type Post } from '@/services/organization';
import type { DepartmentTree } from '@/services/organization';
import type { EmployeeBrief, EmployeeQuery } from '@/services/employee';
import { deleteEmployee, getEmployeeList } from '@/services/employee';
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  MoreOutlined,
  PlusOutlined,
  ReloadOutlined,
  SwapOutlined,
} from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Button, Dropdown, message, Modal, Tag } from 'antd';
import React, { useEffect, useRef, useState } from 'react';

/** 在职状态映射（后端返回数字） */
const STATUS_MAP: Record<number, { label: string; color: string }> = {
  1: { label: '试用期', color: 'orange' },
  2: { label: '正式', color: 'blue' },
  3: { label: '待离职', color: 'volcano' },
  4: { label: '已离职', color: 'red' },
};

/** 职级选项 */
const GRADE_OPTIONS = [
  { label: 'P1', value: 'P1' },
  { label: 'P2', value: 'P2' },
  { label: 'P3', value: 'P3' },
  { label: 'P4', value: 'P4' },
  { label: 'P5', value: 'P5' },
  { label: 'P6', value: 'P6' },
  { label: 'P7', value: 'P7' },
  { label: 'P8', value: 'P8' },
  { label: 'M1', value: 'M1' },
  { label: 'M2', value: 'M2' },
  { label: 'M3', value: 'M3' },
];

const EmployeePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [departments, setDepartments] = useState<DepartmentTree[]>([]);
  const [positions, setPositions] = useState<Post[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);

  /** 加载部门树和职位列表 */
  useEffect(() => {
    getDepartmentTree()
      .then((data) => data && setDepartments(data))
      .catch(() => {});
    getPostList()
      .then((data) => data?.records && setPositions(data.records))
      .catch(() => {});
  }, []);

  /** 删除确认 */
  const handleDelete = (id: number, name: string) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除员工「${name}」吗？删除后不可恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteEmployee(id);
          message.success('删除成功');
          actionRef.current?.reload();
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const columns: ProColumns<EmployeeBrief>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: { placeholder: '姓名 / 工号 / 手机号' },
    },
    {
      title: '姓名',
      dataIndex: 'employeeName',
      width: 120,
      fixed: 'left',
      ellipsis: true,
      search: false,
    },
    {
      title: '工号',
      dataIndex: 'employeeNo',
      width: 120,
      ellipsis: true,
      search: false,
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 140,
      ellipsis: true,
      search: false,
    },
    {
      title: '职位',
      dataIndex: 'postName',
      width: 120,
      ellipsis: true,
      search: false,
    },
    {
      title: '职级',
      dataIndex: 'jobLevel',
      width: 80,
      search: false,
    },
    {
      title: '在职状态',
      dataIndex: 'employmentStatus',
      width: 100,
      valueType: 'select',
      valueEnum: {
        1: { text: '试用期' },
        2: { text: '正式' },
        3: { text: '待离职' },
        4: { text: '已离职' },
      },
      render: (_, record) => {
        const info = STATUS_MAP[record.employmentStatus];
        return (
          <Tag color={info?.color || 'default'}>
            {info?.label || record.employmentStatus}
          </Tag>
        );
      },
    },
    {
      title: '入职日期',
      dataIndex: 'hireDate',
      width: 120,
      valueType: 'date',
      search: false,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      hideInSearch: true,
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => history.push(`/employee/detail/${record.id}`)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => history.push(`/employee/${record.id}/edit`)}
          >
            编辑
          </Button>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'transfer',
                  icon: <SwapOutlined />,
                  label: '调岗',
                  onClick: () =>
                    history.push(
                      `/process/transfer?employeeId=${record.id}&employeeName=${encodeURIComponent(record.employeeName)}&employeeNo=${record.employeeNo || ''}`,
                    ),
                },
                {
                  key: 'delete',
                  icon: <DeleteOutlined />,
                  label: '删除',
                  danger: true,
                  onClick: () => handleDelete(record.id, record.employeeName),
                },
              ],
            }}
            trigger={['click']}
          >
            <Button type="link" size="small" icon={<MoreOutlined />} />
          </Dropdown>
        </div>
      ),
    },
  ];

  return (
    <PageContainer>
      <ProTable<EmployeeBrief, EmployeeQuery>
        headerTitle="员工列表"
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 'auto',
          defaultCollapsed: false,
        }}
        columns={columns}
        request={async (params) => {
          const { current, pageSize, keyword, employmentStatus } = params;
          const query: EmployeeQuery = {
            pageNum: current,
            pageSize,
          };
          // 关键词搜索
          if (keyword) {
            query.keyword = keyword;
          }
          // 在职状态：后端期望数组格式
          if (employmentStatus !== undefined && employmentStatus !== null && employmentStatus !== '') {
            query.employmentStatus = [Number(employmentStatus)];
          }
          const data = await getEmployeeList(query);
          if (!data) {
            return { data: [], total: 0, success: true };
          }
          return {
            data: data.records || [],
            total: data.total || 0,
            success: true,
          };
        }}
        rowSelection={{
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys as number[]),
        }}
        tableAlertRender={({ selectedRowKeys, onCleanSelected }) => (
          <span>
            已选择 {selectedRowKeys.length} 项
            <a style={{ marginLeft: 8 }} onClick={onCleanSelected}>
              取消选择
            </a>
          </span>
        )}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => history.push('/employee/create')}
          >
            新增员工
          </Button>,
          <Button
            key="reload"
            icon={<ReloadOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
        ]}
        // 高级搜索表单
        form={{
          syncToUrl: false,
        }}
        tableExtraRender={() => (
          <>
            {/* 手动渲染高级搜索区域，与 ProTable search 互补 */}
          </>
        )}
      />
    </PageContainer>
  );
};

export default EmployeePage;

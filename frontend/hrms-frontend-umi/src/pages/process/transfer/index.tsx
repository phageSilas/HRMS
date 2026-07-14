/**
 * 调岗申请页面。
 * 接入调岗申请分页和创建接口。
 */

import {
  ApprovalStatus,
  createTransferApplication,
  getTransferApplicationList,
} from '@/services/process';
import type {
  TransferApplication,
  TransferApplicationCreateRequest,
} from '@/services/process';
import { PlusOutlined } from '@ant-design/icons';
import {
  DrawerForm,
  PageContainer,
  ProFormDatePicker,
  ProFormDigit,
  ProFormGroup,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Space, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'processing' },
  [ApprovalStatus.APPROVED]: { text: '已通过', color: 'success' },
  [ApprovalStatus.REJECTED]: { text: '已拒绝', color: 'error' },
  [ApprovalStatus.WITHDRAWN]: { text: '已撤回', color: 'default' },
};

const departmentOptions = [
  { label: '人力资源部', value: 1 },
  { label: '技术部', value: 2 },
  { label: '产品部', value: 3 },
  { label: '财务部', value: 4 },
  { label: '平台架构部', value: 5 },
];

const positionOptions = [
  { label: 'HR 专员', value: 101 },
  { label: 'Java 开发工程师', value: 102 },
  { label: '前端开发工程师', value: 103 },
  { label: '产品经理', value: 104 },
  { label: '高级开发工程师', value: 201 },
  { label: '技术负责人', value: 202 },
];

const leaderOptions = [
  { label: '王敏（1001）', value: 1001 },
  { label: '李强（1002）', value: 1002 },
  { label: '赵琳（1003）', value: 1003 },
];

const TransferPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);

  const columns: ProColumns<TransferApplication>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: { placeholder: '员工姓名 / 工号' },
    },
    {
      title: '原部门',
      dataIndex: 'departmentId',
      hideInTable: true,
      valueType: 'select',
      fieldProps: { options: departmentOptions, allowClear: true },
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      hideInTable: true,
      valueType: 'select',
      valueEnum: {
        [ApprovalStatus.DRAFT]: { text: '草稿' },
        [ApprovalStatus.APPROVING]: { text: '审批中' },
        [ApprovalStatus.APPROVED]: { text: '已通过' },
        [ApprovalStatus.REJECTED]: { text: '已拒绝' },
      },
    },
    {
      title: '员工',
      dataIndex: 'employeeName',
      width: 150,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <strong>{record.employeeName || `员工 ${record.employeeId}`}</strong>
          <span style={{ color: '#6b7280', fontSize: 12 }}>
            {record.employeeNo || `ID ${record.employeeId}`}
          </span>
        </Space>
      ),
    },
    { title: '原部门', dataIndex: 'fromDeptName', width: 120, search: false },
    { title: '原职位', dataIndex: 'fromPostName', width: 160, search: false },
    { title: '新部门', dataIndex: 'toDeptName', width: 120, search: false },
    { title: '新职位', dataIndex: 'toPostName', width: 160, search: false },
    {
      title: '生效日期',
      dataIndex: 'effectiveDate',
      valueType: 'date',
      width: 120,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 110,
      search: false,
      render: (_, record) => {
        const meta = statusMeta[record.approvalStatus ?? ApprovalStatus.DRAFT] || statusMeta[0];
        return <Tag color={meta.color}>{record.approvalStatusDesc || meta.text}</Tag>;
      },
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      width: 170,
      search: false,
    },
  ];

  return (
    <PageContainer
      header={{
        title: '调岗申请',
        subTitle: '原岗位与新岗位对比、审批发起与生效跟踪',
      }}
    >
      <ProTable<TransferApplication>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        scroll={{ x: 1180 }}
        request={async (params) => {
          const result = await getTransferApplicationList({
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword: params.keyword as string,
            departmentId: params.departmentId as number,
            approvalStatus: params.approvalStatus as number,
          });
          return {
            data: result.records || [],
            total: result.total || 0,
            success: true,
          };
        }}
        search={{ labelWidth: 88, span: 8 }}
        pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        toolbar={{
          title: '调岗申请列表',
          actions: [
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setDrawerOpen(true)}
            >
              新建调岗申请
            </Button>,
          ],
        }}
      />

      <DrawerForm<TransferApplicationCreateRequest & {
        employeeName?: string;
        employeeNo?: string;
        fromDeptName?: string;
        fromPostName?: string;
      }>
        title="新建调岗申请"
        width={760}
        open={drawerOpen}
        onOpenChange={setDrawerOpen}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onFinish={async (values) => {
          const payload: TransferApplicationCreateRequest = {
            employeeId: values.employeeId,
            toDeptId: values.toDeptId,
            toPostId: values.toPostId,
            toJobLevel: values.toJobLevel,
            toLeaderId: values.toLeaderId,
            effectiveDate: values.effectiveDate,
            salaryAdjustment: values.salaryAdjustment,
            reason: values.reason,
          };
          await createTransferApplication(payload);
          message.success('调岗申请已提交');
          setDrawerOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormGroup title="员工与原岗位">
          <ProFormDigit
            name="employeeId"
            label="员工 ID"
            width="sm"
            min={1}
            rules={[{ required: true, message: '请输入调岗员工 ID' }]}
          />
          <ProFormText name="employeeName" label="员工姓名" width="md" />
          <ProFormText name="employeeNo" label="员工工号" width="md" />
          <ProFormText name="fromDeptName" label="原部门" width="md" disabled />
          <ProFormText name="fromPostName" label="原职位" width="md" disabled />
        </ProFormGroup>
        <ProFormGroup title="新岗位">
          <ProFormSelect
            name="toDeptId"
            label="新部门"
            width="md"
            options={departmentOptions}
            rules={[{ required: true, message: '请选择新部门' }]}
          />
          <ProFormSelect
            name="toPostId"
            label="新职位"
            width="md"
            options={positionOptions}
            rules={[{ required: true, message: '请选择新职位' }]}
          />
          <ProFormSelect
            name="toJobLevel"
            label="新职级"
            width="sm"
            options={[
              { label: 'P5', value: 'P5' },
              { label: 'P6', value: 'P6' },
              { label: 'P7', value: 'P7' },
            ]}
          />
          <ProFormSelect
            name="toLeaderId"
            label="新汇报人"
            width="md"
            options={leaderOptions}
          />
          <ProFormDatePicker
            name="effectiveDate"
            label="生效日期"
            width="md"
            rules={[{ required: true, message: '请选择生效日期' }]}
          />
          <ProFormDigit
            name="salaryAdjustment"
            label="薪资调整"
            width="sm"
            fieldProps={{ precision: 2 }}
          />
        </ProFormGroup>
        <ProFormTextArea
          name="reason"
          label="调岗原因"
          fieldProps={{ rows: 4 }}
          rules={[{ required: true, message: '请输入调岗原因' }]}
        />
      </DrawerForm>
    </PageContainer>
  );
};

export default TransferPage;

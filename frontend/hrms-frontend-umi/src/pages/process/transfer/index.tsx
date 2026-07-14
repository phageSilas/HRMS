/**
 * 调岗申请页面。
 * 当前提供原岗位/新岗位对比表单，后续接入 /api/v1/transfer-applications。
 */

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
import type { ProColumns } from '@ant-design/pro-components';
import { PlusOutlined } from '@ant-design/icons';
import { Button, Space, Tag, message } from 'antd';
import React, { useState } from 'react';

interface TransferRow {
  id: number;
  employeeName: string;
  employeeNo: string;
  fromDeptName: string;
  fromPostName: string;
  toDeptName: string;
  toPostName: string;
  effectiveDate: string;
  approvalStatus: number;
  reason: string;
  createTime: string;
}

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已通过', color: 'success' },
  3: { text: '已拒绝', color: 'error' },
};

const sampleRows: TransferRow[] = [
  {
    id: 1,
    employeeName: '刘洋',
    employeeNo: 'EMP000128',
    fromDeptName: '技术部',
    fromPostName: 'Java 开发工程师',
    toDeptName: '平台架构部',
    toPostName: '高级开发工程师',
    effectiveDate: '2026-08-01',
    approvalStatus: 1,
    reason: '业务线调整与岗位晋升',
    createTime: '2026-07-12 10:18:00',
  },
];

const departmentOptions = [
  { label: '技术部', value: 2 },
  { label: '平台架构部', value: 5 },
  { label: '产品部', value: 3 },
  { label: '人力资源部', value: 1 },
];

const positionOptions = [
  { label: '高级开发工程师', value: 201 },
  { label: '技术负责人', value: 202 },
  { label: '产品经理', value: 203 },
  { label: 'HR 专员', value: 204 },
];

const employeeOptions = [
  { label: '刘洋（EMP000128）', value: 128 },
  { label: '陈辰（EMP000109）', value: 109 },
  { label: '张晓雨（EMP000137）', value: 137 },
];

const TransferPage: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);

  const columns: ProColumns<TransferRow>[] = [
    {
      title: '员工',
      dataIndex: 'employeeName',
      width: 150,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <strong>{record.employeeName}</strong>
          <span style={{ color: '#6b7280', fontSize: 12 }}>{record.employeeNo}</span>
        </Space>
      ),
    },
    { title: '原部门', dataIndex: 'fromDeptName', width: 120 },
    { title: '原职位', dataIndex: 'fromPostName', width: 160 },
    { title: '新部门', dataIndex: 'toDeptName', width: 120 },
    { title: '新职位', dataIndex: 'toPostName', width: 160 },
    { title: '生效日期', dataIndex: 'effectiveDate', valueType: 'date', width: 120 },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 110,
      render: (_, record) => {
        const meta = statusMap[record.approvalStatus] || statusMap[0];
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    { title: '申请时间', dataIndex: 'createTime', valueType: 'dateTime', width: 170 },
  ];

  return (
    <PageContainer
      header={{
        title: '调岗申请',
        subTitle: '原岗位与新岗位对比、审批发起与生效跟踪',
      }}
    >
      <ProTable<TransferRow>
        rowKey="id"
        columns={columns}
        dataSource={sampleRows}
        search={{
          labelWidth: 88,
        }}
        pagination={false}
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

      <DrawerForm
        title="新建调岗申请"
        width={760}
        open={drawerOpen}
        onOpenChange={setDrawerOpen}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onFinish={async () => {
          message.info('调岗接口尚未接入，已保留提交审批入口');
          setDrawerOpen(false);
          return true;
        }}
      >
        <ProFormGroup title="员工与原岗位">
          <ProFormSelect
            name="employeeId"
            label="员工"
            width="md"
            options={employeeOptions}
            rules={[{ required: true, message: '请选择调岗员工' }]}
          />
          <ProFormText
            name="fromDeptName"
            label="原部门"
            width="md"
            disabled
            initialValue="技术部"
          />
          <ProFormText
            name="fromPostName"
            label="原职位"
            width="md"
            disabled
            initialValue="Java 开发工程师"
          />
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
            options={employeeOptions}
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

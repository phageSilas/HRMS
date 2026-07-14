/**
 * 离职申请页面。
 * 当前提供离职申请基础界面，后续接入 /api/v1/leave-applications。
 */

import {
  DrawerForm,
  PageContainer,
  ProFormDatePicker,
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

interface LeaveRow {
  id: number;
  employeeName: string;
  employeeNo: string;
  departmentName: string;
  leaveTypeName: string;
  lastWorkDate: string;
  leaveDate: string;
  handoverEmployeeName: string;
  approvalStatus: number;
  createTime: string;
}

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已通过', color: 'success' },
  3: { text: '已拒绝', color: 'error' },
  5: { text: '已离职', color: 'cyan' },
};

const sampleRows: LeaveRow[] = [
  {
    id: 1,
    employeeName: '周宁',
    employeeNo: 'EMP000086',
    departmentName: '产品部',
    leaveTypeName: '主动离职',
    lastWorkDate: '2026-07-31',
    leaveDate: '2026-08-01',
    handoverEmployeeName: '陈辰',
    approvalStatus: 1,
    createTime: '2026-07-11 15:22:00',
  },
];

const employeeOptions = [
  { label: '周宁（EMP000086）', value: 86 },
  { label: '刘洋（EMP000128）', value: 128 },
  { label: '张晓雨（EMP000137）', value: 137 },
];

const handoverOptions = [
  { label: '陈辰（产品部）', value: 109 },
  { label: '李强（技术部）', value: 110 },
  { label: '王敏（人力资源部）', value: 111 },
];

const LeavePage: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);

  const columns: ProColumns<LeaveRow>[] = [
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
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '离职类型', dataIndex: 'leaveTypeName', width: 120 },
    { title: '最后工作日', dataIndex: 'lastWorkDate', valueType: 'date', width: 130 },
    { title: '离职日期', dataIndex: 'leaveDate', valueType: 'date', width: 120 },
    { title: '交接人', dataIndex: 'handoverEmployeeName', width: 120 },
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
        title: '离职申请',
        subTitle: '离职申请、工作交接与离职生效跟踪',
      }}
    >
      <ProTable<LeaveRow>
        rowKey="id"
        columns={columns}
        dataSource={sampleRows}
        pagination={false}
        search={{ labelWidth: 88 }}
        toolbar={{
          title: '离职申请列表',
          actions: [
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setDrawerOpen(true)}
            >
              新建离职申请
            </Button>,
          ],
        }}
      />

      <DrawerForm
        title="新建离职申请"
        width={700}
        open={drawerOpen}
        onOpenChange={setDrawerOpen}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onFinish={async () => {
          message.info('离职接口尚未接入，已保留提交审批入口');
          setDrawerOpen(false);
          return true;
        }}
      >
        <ProFormGroup title="员工信息">
          <ProFormSelect
            name="employeeId"
            label="离职员工"
            width="md"
            options={employeeOptions}
            rules={[{ required: true, message: '请选择离职员工' }]}
          />
          <ProFormText
            name="departmentName"
            label="当前部门"
            width="md"
            disabled
            initialValue="产品部"
          />
          <ProFormText
            name="positionName"
            label="当前职位"
            width="md"
            disabled
            initialValue="产品经理"
          />
        </ProFormGroup>

        <ProFormGroup title="离职安排">
          <ProFormSelect
            name="leaveType"
            label="离职类型"
            width="md"
            options={[
              { label: '主动离职', value: 'resign' },
              { label: '公司辞退', value: 'terminate' },
              { label: '协商解除', value: 'mutual' },
              { label: '合同到期', value: 'contract_end' },
            ]}
            rules={[{ required: true, message: '请选择离职类型' }]}
          />
          <ProFormDatePicker
            name="lastWorkDate"
            label="最后工作日"
            width="md"
            rules={[{ required: true, message: '请选择最后工作日' }]}
          />
          <ProFormDatePicker
            name="leaveDate"
            label="离职日期"
            width="md"
            rules={[{ required: true, message: '请选择离职日期' }]}
          />
          <ProFormSelect
            name="handoverEmployeeId"
            label="工作交接人"
            width="md"
            options={handoverOptions}
            rules={[{ required: true, message: '请选择工作交接人' }]}
          />
        </ProFormGroup>

        <ProFormTextArea
          name="reason"
          label="离职原因"
          fieldProps={{ rows: 4 }}
          rules={[{ required: true, message: '请输入离职原因' }]}
        />
      </DrawerForm>
    </PageContainer>
  );
};

export default LeavePage;

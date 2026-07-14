/**
 * 离职申请页面。
 * 接入离职申请分页和创建接口。
 */

import {
  ApprovalStatus,
  createLeaveApplication,
  getLeaveApplicationList,
} from '@/services/process';
import type {
  LeaveApplication,
  LeaveApplicationCreateRequest,
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
  [ApprovalStatus.ENTERED]: { text: '已离职', color: 'cyan' },
};

const leaveTypeOptions = [
  { label: '主动离职', value: 'resign' },
  { label: '公司辞退', value: 'terminate' },
  { label: '协商解除', value: 'mutual' },
  { label: '合同到期', value: 'contract_end' },
];

const departmentOptions = [
  { label: '人力资源部', value: 1 },
  { label: '技术部', value: 2 },
  { label: '产品部', value: 3 },
  { label: '财务部', value: 4 },
];

const handoverOptions = [
  { label: '王敏（1001）', value: 1001 },
  { label: '李强（1002）', value: 1002 },
  { label: '赵琳（1003）', value: 1003 },
];

const LeavePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);

  const columns: ProColumns<LeaveApplication>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: { placeholder: '员工姓名 / 工号' },
    },
    {
      title: '部门',
      dataIndex: 'departmentId',
      hideInTable: true,
      valueType: 'select',
      fieldProps: { options: departmentOptions, allowClear: true },
    },
    {
      title: '离职类型',
      dataIndex: 'leaveType',
      hideInTable: true,
      valueType: 'select',
      fieldProps: { options: leaveTypeOptions, allowClear: true },
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
            ID {record.employeeId}
          </span>
        </Space>
      ),
    },
    { title: '部门', dataIndex: 'departmentName', width: 120, search: false },
    {
      title: '离职类型',
      dataIndex: 'leaveTypeName',
      width: 120,
      search: false,
      renderText: (_, record) =>
        record.leaveTypeName ||
        leaveTypeOptions.find((item) => item.value === record.leaveType)?.label ||
        record.leaveType,
    },
    {
      title: '最后工作日',
      dataIndex: 'lastWorkDate',
      valueType: 'date',
      width: 130,
      search: false,
    },
    {
      title: '离职日期',
      dataIndex: 'leaveDate',
      valueType: 'date',
      width: 120,
      search: false,
    },
    {
      title: '交接人',
      dataIndex: 'handoverEmployeeName',
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
        title: '离职申请',
        subTitle: '离职申请、工作交接与离职生效跟踪',
      }}
    >
      <ProTable<LeaveApplication>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        scroll={{ x: 1120 }}
        request={async (params) => {
          const result = await getLeaveApplicationList({
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword: params.keyword as string,
            departmentId: params.departmentId as number,
            leaveType: params.leaveType as string,
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

      <DrawerForm<LeaveApplicationCreateRequest & {
        employeeName?: string;
        departmentName?: string;
        positionName?: string;
      }>
        title="新建离职申请"
        width={700}
        open={drawerOpen}
        onOpenChange={setDrawerOpen}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onFinish={async (values) => {
          const payload: LeaveApplicationCreateRequest = {
            employeeId: values.employeeId,
            leaveType: values.leaveType,
            leaveReason: values.leaveReason,
            lastWorkDate: values.lastWorkDate,
            handoverEmployeeId: values.handoverEmployeeId,
            remark: values.remark,
          };
          await createLeaveApplication(payload);
          message.success('离职申请已提交');
          setDrawerOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormGroup title="员工信息">
          <ProFormDigit
            name="employeeId"
            label="员工 ID"
            width="sm"
            min={1}
            rules={[{ required: true, message: '请输入离职员工 ID' }]}
          />
          <ProFormText name="employeeName" label="员工姓名" width="md" />
          <ProFormText name="departmentName" label="当前部门" width="md" disabled />
          <ProFormText name="positionName" label="当前职位" width="md" disabled />
        </ProFormGroup>

        <ProFormGroup title="离职安排">
          <ProFormSelect
            name="leaveType"
            label="离职类型"
            width="md"
            options={leaveTypeOptions}
            rules={[{ required: true, message: '请选择离职类型' }]}
          />
          <ProFormDatePicker
            name="lastWorkDate"
            label="最后工作日"
            width="md"
            rules={[{ required: true, message: '请选择最后工作日' }]}
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
          name="leaveReason"
          label="离职原因"
          fieldProps={{ rows: 4 }}
          rules={[{ required: true, message: '请输入离职原因' }]}
        />
        <ProFormTextArea
          name="remark"
          label="备注"
          fieldProps={{ rows: 3 }}
        />
      </DrawerForm>
    </PageContainer>
  );
};

export default LeavePage;

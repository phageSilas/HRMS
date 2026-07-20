/**
 * 离职申请页面。
 * 对接离职申请分页和创建接口。
 */

import { getDeptList } from '@/services/organization';
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
import { Avatar, Button, Card, Space, Tag, Typography, message } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatProcessDateTime } from '../utils';

const { Text } = Typography;

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'processing' },
  [ApprovalStatus.APPROVED]: { text: '已通过', color: 'success' },
  [ApprovalStatus.REJECTED]: { text: '已驳回', color: 'error' },
  [ApprovalStatus.WITHDRAWN]: { text: '已撤回', color: 'default' },
  [ApprovalStatus.ENTERED]: { text: '已离职', color: 'cyan' },
};

const leaveTypeOptions = [
  { label: '主动辞职', value: 'resign' },
  { label: '公司辞退', value: 'terminate' },
  { label: '协商解除', value: 'mutual' },
  { label: '合同到期', value: 'contract_end' },
];

const handoverOptions = [
  { label: '王敏（1001）', value: 1001 },
  { label: '李强（1002）', value: 1002 },
  { label: '赵玲（1003）', value: 1003 },
];

type LeaveFormValues = LeaveApplicationCreateRequest & {
  employeeName?: string;
  departmentName?: string;
  positionName?: string;
};

function getInitial(name?: string) {
  return name?.slice(0, 1) || '员';
}

const LeavePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [departmentOptions, setDepartmentOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [employeePreview, setEmployeePreview] = useState<{
    employeeName?: string;
    employeeId?: number;
    departmentName?: string;
    positionName?: string;
  }>({});

  useEffect(() => {
    const loadDepartments = async () => {
      try {
        const departments = await getDeptList();
        setDepartmentOptions(
          (departments || []).map((item) => ({
            label: item.deptName,
            value: item.id,
          })),
        );
      } catch (error) {
        message.error('部门数据加载失败，请刷新后重试');
      }
    };
    loadDepartments();
  }, []);

  const departmentFilterOption = useMemo(
    () =>
      (input: string, option?: { label?: string | number }) =>
        String(option?.label || '')
          .toLowerCase()
          .includes(input.trim().toLowerCase()),
    [],
  );

  const columns: ProColumns<LeaveApplication>[] = [
    {
      title: '员工姓名',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: {
        placeholder: '请输入员工姓名 / 工号',
        allowClear: true,
      },
    },
    {
      title: '部门',
      dataIndex: 'departmentId',
      hideInTable: true,
      valueType: 'select',
      fieldProps: {
        options: departmentOptions,
        allowClear: true,
        showSearch: true,
        filterOption: departmentFilterOption,
        optionFilterProp: 'label',
        placeholder: '请输入部门名称',
      },
    },
    {
      title: '离职类型',
      dataIndex: 'leaveType',
      hideInTable: true,
      valueType: 'select',
      fieldProps: {
        options: leaveTypeOptions,
        allowClear: true,
        placeholder: '请选择离职类型',
      },
    },
    {
      title: '离职状态',
      dataIndex: 'approvalStatus',
      hideInTable: true,
      valueType: 'select',
      valueEnum: {
        [ApprovalStatus.DRAFT]: { text: '草稿' },
        [ApprovalStatus.APPROVING]: { text: '审批中' },
        [ApprovalStatus.APPROVED]: { text: '已通过' },
        [ApprovalStatus.REJECTED]: { text: '已驳回' },
      },
    },
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      width: 150,
      search: false,
      render: (_, record) => (
        <Space>
          <Avatar style={{ background: '#2f6fed' }}>{getInitial(record.employeeName)}</Avatar>
          <Space direction="vertical" size={0}>
            <strong>{record.employeeName || `员工 ${record.employeeId}`}</strong>
            <Text type="secondary">ID {record.employeeId}</Text>
          </Space>
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
      width: 170,
      search: false,
      render: (_, record) => formatProcessDateTime(record.createTime),
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
              onClick={() => {
                setEmployeePreview({});
                setDrawerOpen(true);
              }}
            >
              创建离职
            </Button>,
          ],
        }}
      />

      <DrawerForm<LeaveFormValues>
        title="离职申请表单"
        width={420}
        open={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            setEmployeePreview({});
          }
        }}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onValuesChange={(_, values) => {
          setEmployeePreview({
            employeeId: values.employeeId,
            employeeName: values.employeeName,
            departmentName: values.departmentName,
            positionName: values.positionName,
          });
        }}
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
        <Card size="small" title="员工信息" style={{ marginBottom: 16 }}>
          <Space>
            <Avatar size={40} style={{ background: '#2f6fed' }}>
              {getInitial(employeePreview.employeeName)}
            </Avatar>
            <Space direction="vertical" size={0}>
              <strong>{employeePreview.employeeName || '待选择员工'}</strong>
              <Text type="secondary">员工 ID：{employeePreview.employeeId || '-'}</Text>
              <Text type="secondary">
                部门：{employeePreview.departmentName || '-'}　职位：
                {employeePreview.positionName || '-'}
              </Text>
            </Space>
          </Space>
        </Card>

        <ProFormGroup>
          <ProFormDigit
            name="employeeId"
            label="员工 ID"
            width="sm"
            min={1}
            rules={[{ required: true, message: '请输入离职员工 ID' }]}
          />
          <ProFormText name="employeeName" label="员工姓名" width="md" />
          <ProFormText name="departmentName" label="当前部门" width="md" />
          <ProFormText name="positionName" label="当前职位" width="md" />
        </ProFormGroup>

        <ProFormSelect
          name="leaveType"
          label="离职类型"
          options={leaveTypeOptions}
          rules={[{ required: true, message: '请选择离职类型' }]}
        />
        <ProFormDatePicker
          name="lastWorkDate"
          label="最后工作日"
          rules={[{ required: true, message: '请选择最后工作日' }]}
        />
        <ProFormSelect
          name="handoverEmployeeId"
          label="工作交接人"
          options={handoverOptions}
          rules={[{ required: true, message: '请选择工作交接人' }]}
        />
        <ProFormTextArea
          name="leaveReason"
          label="离职原因"
          fieldProps={{ rows: 4, maxLength: 500, showCount: true }}
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

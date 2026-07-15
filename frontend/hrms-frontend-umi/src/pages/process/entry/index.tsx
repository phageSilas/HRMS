/**
 * 入职申请页面。
 * 对接后端入职申请列表、创建、更新、提交审批、确认入职接口。
 */

import {
  ApprovalStatus,
  confirmEntryApplication,
  createEntryApplication,
  getEntryApplicationList,
  submitEntryApplication,
  updateEntryApplication,
} from '@/services/process';
import type {
  EntryApplication,
  EntryApplicationFormValues,
} from '@/services/process';
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
import { CheckCircleOutlined, EditOutlined, PlusOutlined, SendOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Modal, Popconfirm, Space, Tag, message } from 'antd';
import type { Dayjs } from 'dayjs';
import React, { useRef, useState } from 'react';

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'processing' },
  [ApprovalStatus.APPROVED]: { text: '已通过', color: 'success' },
  [ApprovalStatus.REJECTED]: { text: '已拒绝', color: 'error' },
  [ApprovalStatus.ENTERED]: { text: '已入职', color: 'cyan' },
};

const departmentOptions = [
  { label: '人力资源部', value: 1 },
  { label: '技术部', value: 2 },
  { label: '产品部', value: 3 },
  { label: '财务部', value: 4 },
];

const postOptions = [
  { label: 'HR 专员', value: 101 },
  { label: 'Java 开发工程师', value: 102 },
  { label: '前端开发工程师', value: 103 },
  { label: '产品经理', value: 104 },
  { label: '薪资专员', value: 105 },
];

const leaderOptions = [
  { label: '王敏', value: 1001 },
  { label: '李强', value: 1002 },
  { label: '赵琳', value: 1003 },
];

function formatDateValue(value?: string | Dayjs): string | undefined {
  if (!value) {
    return undefined;
  }
  if (typeof value === 'string') {
    return value;
  }
  return value.format('YYYY-MM-DD');
}

function buildFormValues(values: EntryApplicationFormValues) {
  return {
    ...values,
    expectedHireDate: formatDateValue(values.expectedHireDate) || '',
  };
}

const EntryPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<EntryApplication>();
  const [confirmRecord, setConfirmRecord] = useState<EntryApplication>();
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [confirmForm] = Form.useForm<{ actualHireDate: Dayjs }>();

  const reloadTable = () => actionRef.current?.reload();

  const handleSubmitApproval = async (record: EntryApplication) => {
    await submitEntryApplication(record.id);
    message.success('已提交入职审批');
    reloadTable();
  };

  const handleConfirmEntry = async () => {
    if (!confirmRecord) {
      return;
    }
    const values = await confirmForm.validateFields();
    setConfirmLoading(true);
    try {
      const result = await confirmEntryApplication(confirmRecord.id, {
        actualHireDate: values.actualHireDate.format('YYYY-MM-DD'),
      });
      message.success(`已确认入职，工号：${result.employeeNo}`);
      setConfirmRecord(undefined);
      confirmForm.resetFields();
      reloadTable();
    } finally {
      setConfirmLoading(false);
    }
  };

  const columns: ProColumns<EntryApplication>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: { placeholder: '候选人姓名 / 手机号' },
    },
    {
      title: '部门',
      dataIndex: 'departmentId',
      hideInTable: true,
      valueType: 'select',
      fieldProps: { options: departmentOptions, allowClear: true },
    },
    {
      title: '申请日期',
      dataIndex: 'dateRange',
      hideInTable: true,
      valueType: 'dateRange',
      search: {
        transform: (value: string[]) => ({
          dateStart: value?.[0],
          dateEnd: value?.[1],
        }),
      },
    },
    {
      title: '候选人',
      dataIndex: 'candidateName',
      width: 120,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <strong>{record.candidateName}</strong>
          <span style={{ color: '#6b7280', fontSize: 12 }}>{record.phone}</span>
        </Space>
      ),
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
      search: false,
    },
    {
      title: '入职部门',
      dataIndex: 'deptName',
      width: 130,
      search: false,
      renderText: (_, record) => record.deptName || `部门 ${record.deptId}`,
    },
    {
      title: '职位',
      dataIndex: 'postName',
      width: 150,
      search: false,
      renderText: (_, record) => record.postName || `职位 ${record.postId}`,
    },
    {
      title: '预计入职日期',
      dataIndex: 'expectedHireDate',
      valueType: 'date',
      width: 130,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 110,
      valueType: 'select',
      valueEnum: {
        [ApprovalStatus.DRAFT]: { text: '草稿' },
        [ApprovalStatus.APPROVING]: { text: '审批中' },
        [ApprovalStatus.APPROVED]: { text: '已通过' },
        [ApprovalStatus.REJECTED]: { text: '已拒绝' },
        [ApprovalStatus.ENTERED]: { text: '已入职' },
      },
      render: (_, record) => {
        const meta = statusMeta[record.approvalStatus] || {
          text: record.approvalStatusDesc || '未知',
          color: 'default',
        };
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
    {
      title: '操作',
      valueType: 'option',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space size={8}>
          {record.approvalStatus === ApprovalStatus.DRAFT && (
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => {
                setEditingRecord(record);
                setDrawerOpen(true);
              }}
            >
              编辑
            </Button>
          )}
          {record.approvalStatus === ApprovalStatus.DRAFT && (
            <Popconfirm
              title="提交入职审批"
              description="提交后将进入审批中，草稿内容不可直接编辑。"
              onConfirm={() => handleSubmitApproval(record)}
            >
              <Button size="small" type="link" icon={<SendOutlined />}>
                提交
              </Button>
            </Popconfirm>
          )}
          {record.approvalStatus === ApprovalStatus.APPROVED && (
            <Button
              size="small"
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={() => {
                setConfirmRecord(record);
                confirmForm.setFieldsValue({ actualHireDate: undefined });
              }}
            >
              确认入职
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer
      header={{
        title: '入职申请',
        subTitle: '候选人录入、入职审批与确认入职',
      }}
    >
      <ProTable<EntryApplication>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        scroll={{ x: 1180 }}
        request={async (params) => {
          const result = await getEntryApplicationList({
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword: params.keyword as string,
            approvalStatus: params.approvalStatus as number,
            departmentId: params.departmentId as number,
            dateStart: params.dateStart as string,
            dateEnd: params.dateEnd as string,
          });
          return {
            data: result.records || [],
            total: result.total || 0,
            success: true,
          };
        }}
        pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        search={{ labelWidth: 88, span: 6 }}
        toolbar={{
          title: '入职申请列表',
          actions: [
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setEditingRecord(undefined);
                setDrawerOpen(true);
              }}
            >
              新建入职申请
            </Button>,
          ],
        }}
      />

      <DrawerForm<EntryApplicationFormValues>
        title={editingRecord ? '编辑入职申请' : '新建入职申请'}
        width={720}
        open={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            setEditingRecord(undefined);
          }
        }}
        drawerProps={{ destroyOnClose: true }}
        initialValues={{
          candidateName: editingRecord?.candidateName,
          gender: editingRecord?.gender,
          phone: editingRecord?.phone,
          email: editingRecord?.email,
          idCardNo: editingRecord?.idCardNo,
          deptId: editingRecord?.deptId,
          postId: editingRecord?.postId,
          hireType: editingRecord?.hireType || 1,
          probationMonth: editingRecord?.probationMonth ?? 3,
          probationSalaryRatio: editingRecord?.probationSalaryRatio ?? 80,
          expectedHireDate: editingRecord?.expectedHireDate,
          leaderId: editingRecord?.leaderId,
          remark: editingRecord?.remark,
        }}
        submitter={{ searchConfig: { submitText: '保存草稿' } }}
        onFinish={async (values) => {
          const payload = buildFormValues(values);
          if (editingRecord) {
            await updateEntryApplication(editingRecord.id, payload);
            message.success('入职申请已更新');
          } else {
            await createEntryApplication(payload);
            message.success('入职申请已保存为草稿');
          }
          setDrawerOpen(false);
          reloadTable();
          return true;
        }}
      >
        <ProFormGroup title="候选人信息">
          <ProFormText
            name="candidateName"
            label="候选人姓名"
            width="md"
            rules={[{ required: true, message: '请输入候选人姓名' }]}
          />
          <ProFormSelect
            name="gender"
            label="性别"
            width="sm"
            options={[
              { label: '未知', value: 0 },
              { label: '男', value: 1 },
              { label: '女', value: 2 },
            ]}
          />
          <ProFormText
            name="phone"
            label="手机号"
            width="md"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
            ]}
          />
          <ProFormText name="email" label="邮箱" width="md" />
          <ProFormText name="idCardNo" label="身份证号" width="md" />
        </ProFormGroup>

        <ProFormGroup title="入职信息">
          <ProFormSelect
            name="deptId"
            label="拟入职部门"
            width="md"
            options={departmentOptions}
            rules={[{ required: true, message: '请选择拟入职部门' }]}
          />
          <ProFormSelect
            name="postId"
            label="拟入职职位"
            width="md"
            options={postOptions}
            rules={[{ required: true, message: '请选择拟入职职位' }]}
          />
          <ProFormSelect
            name="hireType"
            label="录用类型"
            width="sm"
            options={[
              { label: '全职', value: 1 },
              { label: '兼职', value: 2 },
              { label: '实习', value: 3 },
            ]}
            rules={[{ required: true, message: '请选择录用类型' }]}
          />
          <ProFormDatePicker
            name="expectedHireDate"
            label="预计入职日期"
            width="md"
            rules={[{ required: true, message: '请选择预计入职日期' }]}
          />
          <ProFormSelect
            name="leaderId"
            label="直接汇报人"
            width="md"
            options={leaderOptions}
          />
        </ProFormGroup>

        <ProFormGroup title="试用期与备注">
          <ProFormDigit
            name="probationMonth"
            label="试用期（月）"
            width="sm"
            min={0}
            max={12}
            rules={[{ required: true, message: '请输入试用期' }]}
          />
          <ProFormDigit
            name="probationSalaryRatio"
            label="试用期薪资比例（%）"
            width="sm"
            min={0}
            max={100}
            fieldProps={{ precision: 2 }}
          />
          <ProFormTextArea
            name="remark"
            label="备注"
            width="xl"
            fieldProps={{ rows: 3, placeholder: '填写特殊录用说明、审批备注等' }}
          />
        </ProFormGroup>
      </DrawerForm>

      <Modal
        title="确认入职"
        open={Boolean(confirmRecord)}
        confirmLoading={confirmLoading}
        onOk={handleConfirmEntry}
        onCancel={() => {
          setConfirmRecord(undefined);
          confirmForm.resetFields();
        }}
        okText="确认入职"
      >
        <Form form={confirmForm} layout="vertical">
          <Form.Item label="候选人">
            <Space direction="vertical" size={0}>
              <strong>{confirmRecord?.candidateName}</strong>
              <span style={{ color: '#6b7280' }}>{confirmRecord?.phone}</span>
            </Space>
          </Form.Item>
          <Form.Item
            name="actualHireDate"
            label="实际入职日期"
            rules={[{ required: true, message: '请选择实际入职日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default EntryPage;

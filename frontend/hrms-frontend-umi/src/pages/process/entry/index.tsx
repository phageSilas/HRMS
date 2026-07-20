/**
 * 入职管理页面。
 * 对接入职申请列表、创建、更新、提交审批、确认入职接口。
 */

import {
  ApprovalStatus,
  confirmEntryApplication,
  createEntryApplication,
  getEntryApplication,
  getEntryApplicationList,
  submitEntryApplication,
  updateEntryApplication,
} from '@/services/process';
import type {
  EntryApplication,
  EntryApplicationFormValues,
} from '@/services/process';
import { getDeptList, getPostList } from '@/services/organization';
import {
  CheckCircleOutlined,
  EditOutlined,
  PlusOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { formatProcessDateTime } from '../utils';
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
import {
  Avatar,
  Button,
  Card,
  DatePicker,
  Form,
  Modal,
  Popconfirm,
  Row,
  Col,
  Space,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import React, { useEffect, useMemo, useRef, useState } from 'react';

const { Text } = Typography;

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'gold' },
  [ApprovalStatus.APPROVED]: { text: '已批准待入职', color: 'blue' },
  [ApprovalStatus.REJECTED]: { text: '已拒绝', color: 'red' },
  [ApprovalStatus.ENTERED]: { text: '已入职', color: 'green' },
};

const departmentOptions = [
  { label: '人力资源部', value: 1 },
  { label: '技术部', value: 2 },
  { label: '产品部', value: 3 },
  { label: '财务部', value: 4 },
  { label: '运营部', value: 5 },
];

const postOptions = [
  { label: 'HR 专员', value: 101 },
  { label: 'Java 开发工程师', value: 102 },
  { label: '前端开发工程师', value: 103 },
  { label: '产品经理', value: 104 },
  { label: '薪资专员', value: 105 },
  { label: '运营专员', value: 106 },
];

const leaderOptions = [
  { label: '王敏', value: 1001 },
  { label: '李强', value: 1002 },
  { label: '赵琳', value: 1003 },
];

const hireTypeOptions = [
  { label: '全职', value: 1 },
  { label: '兼职', value: 2 },
  { label: '实习', value: 3 },
];

const statusTabs = [
  { key: 'all', label: '全部' },
  { key: String(ApprovalStatus.DRAFT), label: '草稿' },
  { key: String(ApprovalStatus.APPROVING), label: '审批中' },
  { key: String(ApprovalStatus.APPROVED), label: '已批准待入职' },
  { key: String(ApprovalStatus.REJECTED), label: '已拒绝' },
  { key: String(ApprovalStatus.ENTERED), label: '已入职' },
];

function parseFormDateValue(
  value?: string | number[] | Dayjs,
): Dayjs | undefined {
  if (!value) {
    return undefined;
  }
  if (dayjs.isDayjs(value)) {
    return value;
  }
  if (Array.isArray(value)) {
    const [year, month, day] = value;
    if (!year || !month || !day) {
      return undefined;
    }
    const parsedDate = dayjs(new Date(year, month - 1, day));
    return parsedDate.isValid() ? parsedDate : undefined;
  }
  const parsedDate = dayjs(value);
  return parsedDate.isValid() ? parsedDate : undefined;
}

function formatDateValue(value?: string | number[] | Dayjs): string | undefined {
  if (!value) {
    return undefined;
  }
  if (dayjs.isDayjs(value)) {
    return value.format('YYYY-MM-DD');
  }
  if (Array.isArray(value)) {
    return parseFormDateValue(value)?.format('YYYY-MM-DD');
  }
  const parsedDate = dayjs(value);
  return parsedDate.isValid() ? parsedDate.format('YYYY-MM-DD') : value;
}

function buildFormValues(values: EntryApplicationFormValues) {
  return {
    ...values,
    expectedHireDate: formatDateValue(values.expectedHireDate) || '',
  };
}

function buildEntryFormInitialValues(record?: EntryApplication): Partial<EntryApplicationFormValues> {
  return {
    candidateName: record?.candidateName,
    gender: record?.gender,
    phone: record?.phone,
    email: record?.email,
    idCardNo: record?.idCardNo,
    deptId: record?.deptId,
    postId: record?.postId,
    hireType: record?.hireType || 1,
    probationMonth: record?.probationMonth ?? 3,
    probationSalaryRatio: record?.probationSalaryRatio ?? 80,
    expectedHireDate: parseFormDateValue(record?.expectedHireDate),
    leaderId: record?.leaderId,
    remark: record?.remark,
  };
}

function getInitial(name?: string) {
  return name?.slice(0, 1) || '人';
}

const EntryPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<EntryApplication>();
  const [confirmRecord, setConfirmRecord] = useState<EntryApplication>();
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [editingLoadingId, setEditingLoadingId] = useState<number>();
  const [activeStatus, setActiveStatus] = useState<string>('all');
  const [statusCounts, setStatusCounts] = useState<Record<string, number>>({});
  const [realDepartmentOptions, setRealDepartmentOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [realPostOptions, setRealPostOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [confirmForm] = Form.useForm<{ actualHireDate: Dayjs }>();
  const [entryForm] = Form.useForm<EntryApplicationFormValues>();

  const statisticCards = useMemo(
    () => [
      {
        label: '草稿',
        value: statusCounts[String(ApprovalStatus.DRAFT)] || 0,
        color: '#6b7280',
        border: '#e5e7eb',
        background: '#ffffff',
      },
      {
        label: '审批中',
        value: statusCounts[String(ApprovalStatus.APPROVING)] || 0,
        color: '#b7791f',
        border: '#fde68a',
        background: '#fffbeb',
      },
      {
        label: '待入职',
        value: statusCounts[String(ApprovalStatus.APPROVED)] || 0,
        color: '#2563eb',
        border: '#bfdbfe',
        background: '#eff6ff',
      },
      {
        label: '已入职',
        value: statusCounts[String(ApprovalStatus.ENTERED)] || 0,
        color: '#16a34a',
        border: '#bbf7d0',
        background: '#f0fdf4',
      },
    ],
    [statusCounts],
  );

  const reloadTable = () => actionRef.current?.reload();

  const handleSubmitApproval = async (record: EntryApplication) => {
    await submitEntryApplication(record.id);
    message.success('已提交入职审批');
    reloadTable();
  };

  const handleEditRecord = async (recordId: number) => {
    setEditingLoadingId(recordId);
    try {
      const detail = await getEntryApplication(recordId);
      setEditingRecord(detail);
      setDrawerOpen(true);
    } finally {
      setEditingLoadingId(undefined);
    }
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

  useEffect(() => {
    if (!drawerOpen) {
      return;
    }
    if (editingRecord) {
      entryForm.setFieldsValue(buildEntryFormInitialValues(editingRecord));
      return;
    }
    entryForm.resetFields();
    entryForm.setFieldsValue(buildEntryFormInitialValues());
  }, [drawerOpen, editingRecord, entryForm]);

  useEffect(() => {
    const loadOrganizationOptions = async () => {
      try {
        const [departments, posts] = await Promise.all([
          getDeptList(),
          getPostList({ pageNum: 1, pageSize: 200 }),
        ]);
        setRealDepartmentOptions(
          (departments || []).map((item) => ({
            label: item.deptName,
            value: item.id,
          })),
        );
        setRealPostOptions(
          (posts.records || []).map((item) => ({
            label: item.postName,
            value: item.id,
          })),
        );
      } catch (error) {
        message.error('部门或职位数据加载失败，请刷新后重试');
      }
    };
    loadOrganizationOptions();
  }, []);

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
      fieldProps: {
        options: realDepartmentOptions,
        allowClear: true,
        showSearch: true,
        optionFilterProp: 'label',
        filterOption: (input: string, option?: { label?: string | number }) =>
          String(option?.label || '')
            .toLowerCase()
            .includes(input.trim().toLowerCase()),
      },
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
      title: '姓名',
      dataIndex: 'candidateName',
      width: 180,
      search: false,
      render: (_, record) => (
        <Space>
          <Avatar style={{ background: '#2f6fed' }}>{getInitial(record.candidateName)}</Avatar>
          <Space direction="vertical" size={0}>
            <strong>{record.candidateName}</strong>
            <Text type="secondary">{record.phone}</Text>
          </Space>
        </Space>
      ),
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 130,
      search: false,
      renderText: (_, record) => record.deptName || '--',
    },
    {
      title: '职位',
      dataIndex: 'postName',
      width: 150,
      search: false,
      renderText: (_, record) => record.postName || '--',
    },
    {
      title: '录用类型',
      dataIndex: 'hireType',
      width: 100,
      search: false,
      render: (_, record) => {
        const label =
          hireTypeOptions.find((item) => item.value === record.hireType)?.label || '全职';
        return <Tag>{label}</Tag>;
      },
    },
    {
      title: '预计入职日期',
      dataIndex: 'expectedHireDate',
      valueType: 'date',
      width: 140,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 140,
      search: false,
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
      width: 170,
      search: false,
      render: (_, record) => formatProcessDateTime(record.createTime),
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
              loading={editingLoadingId === record.id}
              onClick={() => handleEditRecord(record.id)}
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
                提交审批
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
        title: '入职管理',
        subTitle: '管理候选人入职申请与审批流程',
      }}
    >
      <Row gutter={16} style={{ marginBottom: 16 }}>
        {statisticCards.map((item) => (
          <Col xs={24} sm={12} xl={6} key={item.label}>
            <Card
              bordered
              style={{
                borderColor: item.border,
                background: item.background,
                borderRadius: 8,
              }}
            >
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>{item.label}</Text>
                <div style={{ color: item.color, fontSize: 30, fontWeight: 700 }}>
                  {item.value}
                </div>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      <Card bodyStyle={{ padding: '0 20px 20px' }} style={{ borderRadius: 8 }}>
        <Tabs
          activeKey={activeStatus}
          onChange={(key) => {
            setActiveStatus(key);
            setTimeout(() => reloadTable(), 0);
          }}
          items={statusTabs.map((item) => ({
            key: item.key,
            label: `${item.label} ${
              item.key === 'all' ? statusCounts.all || 0 : statusCounts[item.key] || 0
            }`,
          }))}
        />

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
              approvalStatus:
                activeStatus === 'all' ? undefined : Number(activeStatus),
              departmentId: params.departmentId as number,
              dateStart: params.dateStart as string,
              dateEnd: params.dateEnd as string,
            });
            const counts = (result.records || []).reduce<Record<string, number>>(
              (map, item) => {
                const key = String(item.approvalStatus);
                map[key] = (map[key] || 0) + 1;
                return map;
              },
              { all: result.total || 0 },
            );
            setStatusCounts(counts);
            return {
              data: result.records || [],
              total: result.total || 0,
              success: true,
            };
          }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
          search={{ labelWidth: 88, span: 8 }}
          toolbar={{
            title: '入职申请列表',
            actions: [
              <Button
                key="create"
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  setEditingRecord(undefined);
                  entryForm.resetFields();
                  setDrawerOpen(true);
                }}
              >
                新建入职申请
              </Button>,
            ],
          }}
        />
      </Card>

      <DrawerForm<EntryApplicationFormValues>
        form={entryForm}
        title={editingRecord ? '编辑入职申请' : '新建入职申请'}
        width={760}
        open={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            setEditingRecord(undefined);
            entryForm.resetFields();
          }
        }}
        drawerProps={{ destroyOnClose: true }}
        initialValues={buildEntryFormInitialValues(editingRecord)}
        submitter={{ searchConfig: { submitText: '保存草稿' } }}
        onFinishFailed={() => {
          message.warning('请先补全必填项后再保存');
        }}
        onFinish={async (values) => {
          try {
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
          } catch (error) {
            if (error instanceof Error && error.message) {
              message.error(error.message);
            } else {
              message.error('保存失败，请检查表单后重试');
            }
            return false;
          }
        }}
      >
        <ProFormGroup title="候选人基本信息">
          <ProFormText
            name="candidateName"
            label="姓名"
            width="md"
            rules={[{ required: true, message: '请输入姓名' }]}
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
          <ProFormDatePicker
            name="expectedHireDate"
            label="预计入职日期"
            width="md"
            rules={[{ required: true, message: '请选择预计入职日期' }]}
          />
          <ProFormSelect
            name="deptId"
            label="所属部门"
            width="md"
            options={realDepartmentOptions}
            rules={[{ required: true, message: '请选择所属部门' }]}
          />
          <ProFormSelect
            name="postId"
            label="职位"
            width="md"
            options={realPostOptions}
            rules={[{ required: true, message: '请选择职位' }]}
          />
          <ProFormSelect
            name="leaderId"
            label="直接汇报人"
            width="md"
            options={leaderOptions}
          />
        </ProFormGroup>

        <ProFormGroup title="录用类型与试用期">
          <ProFormSelect
            name="hireType"
            label="录用类型"
            width="sm"
            options={hireTypeOptions}
            rules={[{ required: true, message: '请选择录用类型' }]}
          />
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
            <Space>
              <Avatar style={{ background: '#2f6fed' }}>
                {getInitial(confirmRecord?.candidateName)}
              </Avatar>
              <Space direction="vertical" size={0}>
                <strong>{confirmRecord?.candidateName}</strong>
                <Text type="secondary">{confirmRecord?.phone}</Text>
              </Space>
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

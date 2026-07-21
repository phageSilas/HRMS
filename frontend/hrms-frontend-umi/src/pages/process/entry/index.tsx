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
  getEntryApplicationStats,
  submitEntryApplication,
  updateEntryApplication,
} from '@/services/process';
import type {
  EntryApplication,
  EntryApplicationFormValues,
  EntryApplicationQuery,
  EntryApplicationStats,
} from '@/services/process';
import { getEmployeeList } from '@/services/employee';
import { getDeptDetail, getDeptList, getPostList } from '@/services/organization';
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

/** 解析表单日期值，统一兼容字符串、数组和 Dayjs 三种日期来源。 */
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

/** 构建提交给后端的入职表单数据，内部调用 `formatDateValue` 统一日期格式。 */
function buildFormValues(values: EntryApplicationFormValues) {
  return {
    ...values,
    expectedHireDate: formatDateValue(values.expectedHireDate) || '',
  };
}

/** 构建入职表单初始值，内部调用 `parseFormDateValue` 回填日期控件。 */
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

/** 获取姓名首字，用于候选人头像占位展示。 */
function getInitial(name?: string) {
  return name?.slice(0, 1) || '人';
}

/** 生成候选汇报人角色标签，区分 Leader、HR 或二者兼具。 */
function buildLeaderRoleLabel(
  employeeId: number,
  leaderIds: Set<number>,
  postName?: string,
) {
  const isLeader = leaderIds.has(employeeId);
  const isHr = /hr|人力/i.test(postName || '');
  if (isLeader && isHr) {
    return 'Leader / HR';
  }
  if (isLeader) {
    return 'Leader';
  }
  if (isHr) {
    return 'HR';
  }
  return '';
}

/**
 * 入职管理页面组件。
 * 负责入职申请列表、草稿维护、提交审批和确认入职全流程操作。
 */
const EntryPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<EntryApplication>();
  const [confirmRecord, setConfirmRecord] = useState<EntryApplication>();
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [editingLoadingId, setEditingLoadingId] = useState<number>();
  const [activeStatus, setActiveStatus] = useState<string>('all');
  const [statusCounts, setStatusCounts] = useState<EntryApplicationStats>({
    all: 0,
    draft: 0,
    approving: 0,
    approved: 0,
    rejected: 0,
    entered: 0,
  });
  const [realDepartmentOptions, setRealDepartmentOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [realPostOptions, setRealPostOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [realLeaderOptions, setRealLeaderOptions] = useState<
    { label: string; value: number }[]
  >([]);
  const [leaderLoading, setLeaderLoading] = useState(false);
  const [confirmForm] = Form.useForm<{ actualHireDate: Dayjs }>();
  const [entryForm] = Form.useForm<EntryApplicationFormValues>();
  const selectedDeptId = Form.useWatch('deptId', entryForm);

  const statisticCards = useMemo(
    () => [
      {
        label: '草稿',
        value: statusCounts.draft || 0,
        color: '#6b7280',
        border: '#e5e7eb',
        background: '#ffffff',
      },
      {
        label: '审批中',
        value: statusCounts.approving || 0,
        color: '#b7791f',
        border: '#fde68a',
        background: '#fffbeb',
      },
      {
        label: '待入职',
        value: statusCounts.approved || 0,
        color: '#2563eb',
        border: '#bfdbfe',
        background: '#eff6ff',
      },
      {
        label: '已入职',
        value: statusCounts.entered || 0,
        color: '#16a34a',
        border: '#bbf7d0',
        background: '#f0fdf4',
      },
    ],
    [statusCounts],
  );

  const latestBaseQueryRef = useRef<
    Omit<EntryApplicationQuery, 'pageNum' | 'pageSize' | 'approvalStatus'>
  >({});

  /** 加载状态统计卡片数据，供筛选结果变化后同步刷新顶部汇总。 */
  const loadStatusCounts = async (
    query: Omit<EntryApplicationQuery, 'pageNum' | 'pageSize' | 'approvalStatus'>,
  ) => {
    const stats = await getEntryApplicationStats(query);
    setStatusCounts(stats);
  };

  /** 刷新表格数据，供提交审批、保存草稿和确认入职后复用。 */
  const reloadTable = () => actionRef.current?.reload();

  /** 提交入职审批，内部调用 `loadStatusCounts` 刷新统计并调用 `reloadTable` 刷新列表。 */
  const handleSubmitApproval = async (record: EntryApplication) => {
    await submitEntryApplication(record.id);
    message.success('已提交入职审批');
    await loadStatusCounts(latestBaseQueryRef.current);
    reloadTable();
  };

  /** 加载并打开草稿编辑表单，内部调用 `getEntryApplication` 获取详情。 */
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

  /** 确认员工正式入职，内部调用 `loadStatusCounts` 和 `reloadTable` 同步页面状态。 */
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
      await loadStatusCounts(latestBaseQueryRef.current);
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

  useEffect(() => {
    const loadLeaderOptions = async () => {
      if (!drawerOpen || !selectedDeptId) {
        setRealLeaderOptions([]);
        setLeaderLoading(false);
        if (!selectedDeptId) {
          entryForm.setFieldsValue({ leaderId: undefined });
        }
        return;
      }
      setLeaderLoading(true);
      try {
        const currentDept = await getDeptDetail(selectedDeptId);
        const parentDept =
          currentDept.parentId && currentDept.parentId > 0
            ? await getDeptDetail(currentDept.parentId)
            : undefined;
        const targetDeptIds = [
          currentDept.id,
          ...(parentDept?.id ? [parentDept.id] : []),
        ];
        const leaderIds = new Set<number>(
          [currentDept.leaderEmployeeId, parentDept?.leaderEmployeeId].filter(
            (item): item is number => typeof item === 'number' && item > 0,
          ),
        );
        const employeePage = await getEmployeeList({
          deptIds: targetDeptIds,
          pageNum: 1,
          pageSize: 200,
        });
        const options = (employeePage.records || [])
          .filter((employee) => {
            const isLeader = leaderIds.has(employee.id);
            const isHr = /hr|人力/i.test(employee.postName || '');
            return isLeader || isHr;
          })
          .map((employee) => {
            const roleLabel = buildLeaderRoleLabel(
              employee.id,
              leaderIds,
              employee.postName,
            );
            return {
              label: roleLabel
                ? `${employee.employeeName}（${employee.deptName} · ${roleLabel}）`
                : `${employee.employeeName}（${employee.deptName}）`,
              value: employee.id,
            };
          })
          .filter(
            (option, index, array) =>
              array.findIndex((item) => item.value === option.value) === index,
          );
        setRealLeaderOptions(options);
        const currentLeaderId = entryForm.getFieldValue('leaderId');
        if (
          currentLeaderId &&
          !options.some((option) => option.value === currentLeaderId)
        ) {
          entryForm.setFieldsValue({ leaderId: undefined });
        }
      } catch (error) {
        message.error('直接汇报人候选加载失败，请重新选择部门后重试');
        setRealLeaderOptions([]);
      } finally {
        setLeaderLoading(false);
      }
    };
    loadLeaderOptions();
  }, [drawerOpen, selectedDeptId, entryForm]);

  useEffect(() => {
    loadStatusCounts({});
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
            setTimeout(() => actionRef.current?.reloadAndRest?.(), 0);
          }}
          items={statusTabs.map((item) => ({
            key: item.key,
            label: `${item.label} ${
              item.key === 'all'
                ? statusCounts.all || 0
                : statusCounts[
                    item.key === String(ApprovalStatus.DRAFT)
                      ? 'draft'
                      : item.key === String(ApprovalStatus.APPROVING)
                        ? 'approving'
                        : item.key === String(ApprovalStatus.APPROVED)
                          ? 'approved'
                          : item.key === String(ApprovalStatus.REJECTED)
                            ? 'rejected'
                            : 'entered'
                  ] || 0
            }`,
          }))}
        />

        <ProTable<EntryApplication>
          actionRef={actionRef}
          rowKey="id"
          columns={columns}
          scroll={{ x: 1180 }}
          request={async (params) => {
            const baseQuery = {
              keyword: params.keyword as string,
              departmentId: params.departmentId as number,
              dateStart: params.dateStart as string,
              dateEnd: params.dateEnd as string,
            };
            const baseQueryKey = JSON.stringify(baseQuery);
            const previousBaseQueryKey = JSON.stringify(latestBaseQueryRef.current);
            if (baseQueryKey !== previousBaseQueryKey) {
              latestBaseQueryRef.current = baseQuery;
              await loadStatusCounts(baseQuery);
            }
            const result = await getEntryApplicationList({
              pageNum: params.current || 1,
              pageSize: params.pageSize || 20,
              ...baseQuery,
              approvalStatus:
                activeStatus === 'all' ? undefined : Number(activeStatus),
            });
            return {
              data: result.records || [],
              total: result.total || 0,
              success: true,
            };
          }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
          search={{
            labelWidth: 88,
            span: 8,
            onReset: async () => {
              latestBaseQueryRef.current = {};
              await loadStatusCounts({});
            },
          }}
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
            await loadStatusCounts(latestBaseQueryRef.current);
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
            options={realLeaderOptions}
            fieldProps={{
              disabled: !selectedDeptId,
              loading: leaderLoading,
              placeholder: selectedDeptId
                ? '请选择直接汇报人'
                : '请先选择所属部门',
              showSearch: true,
              optionFilterProp: 'label',
            }}
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

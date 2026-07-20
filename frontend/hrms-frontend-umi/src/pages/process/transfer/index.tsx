/**
 * 调岗申请页面。
 * 对接调岗申请分页和创建接口。
 */

import { getDeptList } from '@/services/organization';
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
  ModalForm,
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
import { useSearchParams } from '@umijs/max';
import { Button, Card, Col, Row, Space, Tag, Typography, message } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatProcessDateTime } from '../utils';

const { Text } = Typography;

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'processing' },
  [ApprovalStatus.APPROVED]: { text: '已通过', color: 'success' },
  [ApprovalStatus.REJECTED]: { text: '已驳回', color: 'error' },
  [ApprovalStatus.WITHDRAWN]: { text: '已撤回', color: 'default' },
};

const createDepartmentOptions = [
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
  { label: '赵玲（1003）', value: 1003 },
];

type TransferFormValues = TransferApplicationCreateRequest & {
  employeeName?: string;
  employeeNo?: string;
  fromDeptName?: string;
  fromPostName?: string;
};

const TransferPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [departmentOptions, setDepartmentOptions] = useState<
    { label: string; value: number }[]
  >([]);

  const [searchParams] = useSearchParams();
  const employeeIdFromUrl = searchParams.get('employeeId');
  const employeeNameFromUrl = searchParams.get('employeeName') || '';
  const employeeNoFromUrl = searchParams.get('employeeNo') || '';

  useEffect(() => {
    if (employeeIdFromUrl) {
      setModalOpen(true);
    }
  }, [employeeIdFromUrl]);

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

  const columns: ProColumns<TransferApplication>[] = [
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
      title: '原部门',
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
      title: '调岗状态',
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
      width: 140,
      search: false,
      renderText: (_, record) => record.employeeName || `员工 ${record.employeeId}`,
    },
    {
      title: '原部门 / 新部门',
      dataIndex: 'fromDeptName',
      width: 180,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.fromDeptName || '-'}</span>
          <Text type="secondary">/ {record.toDeptName || '-'}</Text>
        </Space>
      ),
    },
    {
      title: '原职位 / 新职位',
      dataIndex: 'fromPostName',
      width: 200,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.fromPostName || '-'}</span>
          <Text type="secondary">/ {record.toPostName || '-'}</Text>
        </Space>
      ),
    },
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
      width: 170,
      search: false,
      render: (_, record) => formatProcessDateTime(record.createTime),
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
        scroll={{ x: 1080 }}
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
              onClick={() => setModalOpen(true)}
            >
              创建调岗申请
            </Button>,
          ],
        }}
      />

      <ModalForm<TransferFormValues>
        title="创建调岗申请"
        width={760}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnClose: true, centered: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        initialValues={{
          employeeId: employeeIdFromUrl ? Number(employeeIdFromUrl) : undefined,
          employeeName: employeeNameFromUrl || undefined,
          employeeNo: employeeNoFromUrl || undefined,
        }}
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
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormGroup title="员工选择">
          <ProFormDigit
            name="employeeId"
            label="员工 ID"
            width="sm"
            min={1}
            rules={[{ required: true, message: '请输入调岗员工 ID' }]}
          />
          <ProFormText name="employeeName" label="员工姓名" width="md" />
          <ProFormText name="employeeNo" label="员工工号" width="md" />
        </ProFormGroup>

        <Row gutter={16}>
          <Col span={12}>
            <Card size="small" title="原岗位信息（只读）" style={{ minHeight: 252 }}>
              <ProFormText name="fromDeptName" label="原部门" disabled placeholder="员工接口完善后自动带出" />
              <ProFormText name="fromPostName" label="原职位" disabled placeholder="员工接口完善后自动带出" />
            </Card>
          </Col>
          <Col span={12}>
            <Card size="small" title="新岗位信息" style={{ minHeight: 252 }}>
              <ProFormSelect
                name="toDeptId"
                label="新部门"
                options={createDepartmentOptions}
                rules={[{ required: true, message: '请选择新部门' }]}
              />
              <ProFormSelect
                name="toPostId"
                label="新职位"
                options={positionOptions}
                rules={[{ required: true, message: '请选择新职位' }]}
              />
              <ProFormSelect
                name="toJobLevel"
                label="新职级"
                options={[
                  { label: 'P5', value: 'P5' },
                  { label: 'P6', value: 'P6' },
                  { label: 'P7', value: 'P7' },
                ]}
              />
              <ProFormSelect
                name="toLeaderId"
                label="新汇报人"
                options={leaderOptions}
              />
            </Card>
          </Col>
        </Row>

        <ProFormGroup>
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
          fieldProps={{ rows: 4, maxLength: 200, showCount: true }}
          rules={[{ required: true, message: '请输入调岗原因' }]}
        />
      </ModalForm>
    </PageContainer>
  );
};

export default TransferPage;

/**
 * 合同管理页面
 * 负责人：成员 B
 *
 * 功能：合同列表、状态标签、到期提醒、合同详情弹窗
 */

import type { Contract } from '@/services/employee';
import { getContractList } from '@/services/employee';
import {
  EditOutlined,
  EyeOutlined,
  ExclamationCircleOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  PageContainer,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import {
  Badge,
  Button,
  DatePicker,
  Form,
  Input,
  message,
  Modal,
  Select,
  Space,
  Tag,
} from 'antd';
import dayjs from 'dayjs';
import React, { useRef, useState } from 'react';

const { Option } = Select;

const CONTRACT_TYPE_OPTIONS = [
  { label: '劳动合同', value: 'labor' },
  { label: '劳务合同', value: 'service' },
  { label: '实习协议', value: 'intern' },
  { label: '兼职协议', value: 'part_time' },
];

const RENEWAL_STATUS_OPTIONS = [
  { label: '正常', value: 'active' },
  { label: '即将到期', value: 'expiring' },
  { label: '已到期', value: 'expired' },
  { label: '已续签', value: 'renewed' },
];

/** 续签状态颜色 */
const RENEWAL_COLOR: Record<string, string> = {
  active: 'green',
  expiring: 'orange',
  expired: 'red',
  renewed: 'blue',
};

const ContractPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentContract, setCurrentContract] = useState<Contract | null>(null);
  const [editVisible, setEditVisible] = useState(false);
  const [form] = Form.useForm();

  const columns: ProColumns<Contract>[] = [
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      width: 120,
      fixed: 'left',
      ellipsis: true,
    },
    {
      title: '工号',
      dataIndex: 'employeeNo',
      width: 120,
      ellipsis: true,
    },
    {
      title: '合同类型',
      dataIndex: 'contractType',
      width: 120,
      render: (_, record) => {
        const opt = CONTRACT_TYPE_OPTIONS.find(
          (o) => o.value === record.contractType,
        );
        return opt?.label || record.contractType || '-';
      },
    },
    {
      title: '合同开始日期',
      dataIndex: 'contractStartDate',
      width: 130,
      valueType: 'date',
    },
    {
      title: '合同结束日期',
      dataIndex: 'contractEndDate',
      width: 130,
      valueType: 'date',
    },
    {
      title: '到期状态',
      dataIndex: 'renewalStatus',
      width: 120,
      render: (_, record) => {
        const opt = RENEWAL_STATUS_OPTIONS.find(
          (o) => o.value === record.renewalStatus,
        );
        const label = opt?.label || record.renewalStatus || '-';
        const color = RENEWAL_COLOR[record.renewalStatus] || 'default';
        return <Tag color={color}>{label}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      hideInSearch: true,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => {
              setCurrentContract(record);
              setDetailVisible(true);
            }}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setCurrentContract(record);
              form.setFieldsValue({
                ...record,
                contractStartDate: record.contractStartDate
                  ? dayjs(record.contractStartDate)
                  : undefined,
                contractEndDate: record.contractEndDate
                  ? dayjs(record.contractEndDate)
                  : undefined,
              });
              setEditVisible(true);
            }}
          >
            编辑
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <ProTable<Contract>
        headerTitle="合同管理"
        actionRef={actionRef}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        columns={columns}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          const data = await getContractList({
            pageNum: current,
            pageSize,
            ...rest,
          });
          if (!data) return { data: [], total: 0, success: true };
          return {
            data: data.records || [],
            total: data.total || 0,
            success: true,
          };
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setCurrentContract(null);
              form.resetFields();
              setEditVisible(true);
            }}
          >
            新增合同
          </Button>,
          <Button
            key="reload"
            icon={<ReloadOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
        ]}
      />

      {/* 合同详情弹窗 */}
      <Modal
        title="合同详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={560}
      >
        {currentContract && (
          <div>
            <p>
              <strong>员工：</strong>
              {currentContract.employeeName}（{currentContract.employeeNo}）
            </p>
            <p>
              <strong>合同类型：</strong>
              {CONTRACT_TYPE_OPTIONS.find(
                (o) => o.value === currentContract.contractType,
              )?.label || currentContract.contractType}
            </p>
            <p>
              <strong>合同起止：</strong>
              {currentContract.contractStartDate || '-'} ~{' '}
              {currentContract.contractEndDate || '-'}
            </p>
            <p>
              <strong>续签状态：</strong>
              <Tag
                color={RENEWAL_COLOR[currentContract.renewalStatus] || 'default'}
              >
                {RENEWAL_STATUS_OPTIONS.find(
                  (o) => o.value === currentContract.renewalStatus,
                )?.label || currentContract.renewalStatus}
              </Tag>
            </p>
            <p>
              <strong>备注：</strong>
              {currentContract.remark || '-'}
            </p>
          </div>
        )}
      </Modal>

      {/* 合同新增/编辑弹窗 */}
      <Modal
        title={currentContract ? '编辑合同' : '新增合同'}
        open={editVisible}
        onCancel={() => setEditVisible(false)}
        onOk={async () => {
          try {
            const values = await form.validateFields();
            // 使用动态导入的 saveContract
            const { saveContract } = await import('@/services/employee');
            await saveContract({
              ...values,
              id: currentContract?.id,
              contractStartDate: values.contractStartDate
                ? dayjs(values.contractStartDate).format('YYYY-MM-DD')
                : undefined,
              contractEndDate: values.contractEndDate
                ? dayjs(values.contractEndDate).format('YYYY-MM-DD')
                : undefined,
            });
            message.success(currentContract ? '合同已更新' : '合同已创建');
            setEditVisible(false);
            actionRef.current?.reload();
          } catch (err: any) {
            if (err?.errorFields) return;
            message.error('操作失败');
          }
        }}
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="employeeId"
            label="员工 ID"
            rules={[{ required: true, message: '请输入员工 ID' }]}
          >
            <Input placeholder="请输入员工 ID" />
          </Form.Item>
          <Form.Item name="contractType" label="合同类型">
            <Select placeholder="请选择合同类型" allowClear>
              {CONTRACT_TYPE_OPTIONS.map((o) => (
                <Option key={o.value} value={o.value}>
                  {o.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="contractStartDate" label="合同开始日期">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="contractEndDate" label="合同结束日期">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="请输入备注" rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ContractPage;

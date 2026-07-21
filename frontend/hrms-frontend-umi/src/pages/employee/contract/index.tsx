/**
 * 合同管理页面
 *
 * 功能：按员工查询合同、合同 CRUD、合同到期状态标识
 * - admin用户可以查看所有合同
 * - 其他用户需要先选择员工才能查看合同
 *
 * 路由：/employee/contract
 */

import {
  createContract,
  deleteContract,
  getContractsByEmployee,
  getContractDetail,
  getContractList,
  updateContract,
  type Contract,
  type ContractCreateRequest,
  type ContractUpdateRequest,
} from '@/services/employee';
import { getEmployeeList, type EmployeeBrief } from '@/services/employee';
import { RoleCode } from '@/types/user';
import { useModel } from '@umijs/max';
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import {
  Button,
  DatePicker,
  Descriptions,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Space,
  Tag,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useRef, useState } from 'react';

const { Option } = Select;

/** 合同类型映射（与后端 ContractTypeEnum 对齐） */
const CONTRACT_TYPE_MAP: Record<number, { label: string; color: string }> = {
  1: { label: '固定期限', color: 'blue' },
  2: { label: '无固定期限', color: 'green' },
  3: { label: '劳务合同', color: 'orange' },
};

/**
 * 根据合同日期计算到期状态
 * - 已过期：endDate < 今天
 * - 即将到期：30天内到期
 * - 正常：30天以上
 * - 无固定期限：无 endDate
 */
function getContractStatus(endDate?: string): {
  label: string;
  color: string;
} {
  if (!endDate) {
    return { label: '无固定期限', color: 'green' };
  }
  const today = dayjs();
  const end = dayjs(endDate);
  if (end.isBefore(today, 'day')) {
    return { label: '已到期', color: 'red' };
  }
  if (end.diff(today, 'day') <= 30) {
    return { label: '即将到期', color: 'orange' };
  }
  return { label: '正常', color: 'green' };
}

const ContractPage: React.FC = () => {
  const actionRef = useRef<any>();
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState || {};
  const isAdmin = currentUser?.roleCode === RoleCode.ADMIN;

  const [employees, setEmployees] = useState<EmployeeBrief[]>([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<
    number | undefined
  >();
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentContract, setCurrentContract] = useState<Contract | null>(null);
  const [editVisible, setEditVisible] = useState(false);
  const [form] = Form.useForm();
  const [editingContract, setEditingContract] = useState<Contract | null>(null);

  /** 加载员工列表（用于下拉选择） */
  useEffect(() => {
    fetchEmployees('');
  }, []);

  const fetchEmployees = async (keyword: string) => {
    try {
      const data = await getEmployeeList({
        keyword,
        pageNum: 1,
        pageSize: 200,
      });
      if (data?.records) {
        setEmployees(data.records);
      }
    } catch {
      // ignore
    }
  };

  /** 合同表格列定义 */
  const columns: ProColumns<Contract>[] = [
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      width: 120,
      fixed: 'left',
      hideInTable: !isAdmin && !selectedEmployeeId,
    },
    {
      title: '工号',
      dataIndex: 'employeeNo',
      width: 100,
      hideInTable: !isAdmin,
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 120,
      hideInTable: !isAdmin,
    },
    {
      title: '合同编号',
      dataIndex: 'contractNo',
      width: 140,
      ellipsis: true,
      render: (_, record) => record.contractNo || '-',
    },
    {
      title: '合同类型',
      dataIndex: 'contractType',
      width: 110,
      render: (_, record) => {
        const info = CONTRACT_TYPE_MAP[record.contractType];
        return info ? (
          <Tag color={info.color}>{info.label}</Tag>
        ) : (
          record.contractTypeDesc || '-'
        );
      },
    },
    {
      title: '开始日期',
      dataIndex: 'startDate',
      width: 120,
      valueType: 'date',
      render: (_, record) => record.startDate || '-',
    },
    {
      title: '结束日期',
      dataIndex: 'endDate',
      width: 120,
      valueType: 'date',
      render: (_, record) => record.endDate || '-',
    },
    {
      title: '到期状态',
      dataIndex: 'endDate',
      width: 110,
      render: (_, record) => {
        const status = getContractStatus(record.endDate);
        return <Tag color={status.color}>{status.label}</Tag>;
      },
    },
    {
      title: '试用期',
      dataIndex: 'probationMonth',
      width: 80,
      render: (_, record) =>
        record.probationMonth ? `${record.probationMonth} 个月` : '-',
    },
    {
      title: '续签次数',
      dataIndex: 'signingCount',
      width: 80,
      render: (_, record) => record.signingCount ?? '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 160,
      valueType: 'dateTime',
      render: (_, record) => record.createTime || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={async () => {
              try {
                const detail = await getContractDetail(record.id);
                setCurrentContract(detail);
                setDetailVisible(true);
              } catch {
                message.error('获取合同详情失败');
              }
            }}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingContract(record);
              form.setFieldsValue({
                contractNo: record.contractNo || undefined,
                contractType: record.contractType,
                startDate: record.startDate ? dayjs(record.startDate) : undefined,
                endDate: record.endDate ? dayjs(record.endDate) : undefined,
                probationMonth: record.probationMonth,
                probationSalaryRatio: record.probationSalaryRatio,
                remark: record.remark || undefined,
              });
              setEditVisible(true);
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => {
              Modal.confirm({
                title: '确认删除',
                content: '确定要删除该合同记录吗？',
                okType: 'danger',
                onOk: async () => {
                  try {
                    await deleteContract(record.id);
                    message.success('合同已删除');
                    actionRef.current?.reload();
                  } catch {
                    message.error('删除失败');
                  }
                },
              });
            }}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      {/* 顶部：选择员工（非admin用户必须选择） */}
      <div
        style={{
          marginBottom: 16,
          display: 'flex',
          alignItems: 'center',
          gap: 12,
        }}
      >
        <span style={{ fontWeight: 500, whiteSpace: 'nowrap' }}>
          选择员工：
        </span>
        <Select
          showSearch
          placeholder="姓名 / 工号 / 手机号"
          style={{ width: 320 }}
          value={selectedEmployeeId}
          filterOption={false}
          onSearch={(val) => fetchEmployees(val)}
          onChange={(val) => setSelectedEmployeeId(val)}
          allowClear
          notFoundContent={null}
        >
          {employees.map((emp) => (
            <Option key={emp.id} value={emp.id}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 500 }}>{emp.employeeName}</span>
                <span style={{ color: '#999', fontSize: 12 }}>{emp.employeeNo}</span>
                <span style={{ color: '#666', fontSize: 12 }}>{emp.deptName}</span>
              </div>
            </Option>
          ))}
        </Select>
        {isAdmin && (
          <span style={{ color: '#999', fontSize: 12 }}>
            管理员可不选择员工直接查看所有合同
          </span>
        )}
      </div>

      {/* 合同列表表格 */}
      <ProTable<Contract>
        headerTitle={`合同列表${selectedEmployeeId ? '（员工ID: ' + selectedEmployeeId + '）' : ''}`}
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        params={{ employeeId: selectedEmployeeId }}
        request={async (params) => {
          // admin用户可以查看所有合同
          if (isAdmin && !selectedEmployeeId) {
            try {
              const result = await getContractList({
                pageNum: params.current,
                pageSize: params.pageSize,
              });
              return {
                data: result?.records || [],
                total: result?.total || 0,
                success: true,
              };
            } catch {
              return { data: [], total: 0, success: true };
            }
          }

          // 非admin用户或已选择员工
          if (!selectedEmployeeId) {
            return { data: [], total: 0, success: true };
          }
          try {
            const contracts = await getContractsByEmployee(selectedEmployeeId);
            return {
              data: contracts || [],
              total: (contracts || []).length,
              success: true,
            };
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            disabled={!isAdmin && !selectedEmployeeId}
            onClick={() => {
              if (!isAdmin && !selectedEmployeeId) {
                message.warning('请先选择员工');
                return;
              }
              setEditingContract(null);
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
        locale={{
          emptyText: selectedEmployeeId
            ? '该员工暂无合同记录'
            : isAdmin
              ? '暂无合同记录'
              : '请先选择员工查看合同',
        }}
      />

      {/* 合同详情弹窗 */}
      <Modal
        title="合同详情"
        open={detailVisible}
        onCancel={() => {
          setDetailVisible(false);
          setCurrentContract(null);
        }}
        footer={null}
        width={640}
      >
        {currentContract && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="合同编号">
              {currentContract.contractNo || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="合同类型">
              <Tag
                color={
                  CONTRACT_TYPE_MAP[currentContract.contractType]?.color ||
                  'default'
                }
              >
                {currentContract.contractTypeDesc ||
                  CONTRACT_TYPE_MAP[currentContract.contractType]?.label ||
                  '-'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="开始日期">
              {currentContract.startDate || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="结束日期">
              {currentContract.endDate || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="到期状态">
              {(() => {
                const status = getContractStatus(currentContract.endDate);
                return <Tag color={status.color}>{status.label}</Tag>;
              })()}
            </Descriptions.Item>
            <Descriptions.Item label="试用期">
              {currentContract.probationMonth
                ? `${currentContract.probationMonth} 个月`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="试用期薪资比例">
              {currentContract.probationSalaryRatio
                ? `${currentContract.probationSalaryRatio}%`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="续签次数">
              {currentContract.signingCount ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="员工ID">
              {currentContract.employeeId}
            </Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>
              {currentContract.remark || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {currentContract.createTime || '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      {/* 合同新增/编辑弹窗 */}
      <Modal
        title={editingContract ? '编辑合同' : '新增合同'}
        open={editVisible}
        onCancel={() => {
          setEditVisible(false);
          setEditingContract(null);
        }}
        onOk={async () => {
          try {
            const values = await form.validateFields();
            const dateValues = {
              contractNo: values.contractNo,
              contractType: values.contractType,
              startDate: values.startDate
                ? dayjs(values.startDate).format('YYYY-MM-DD')
                : undefined,
              endDate: values.endDate
                ? dayjs(values.endDate).format('YYYY-MM-DD')
                : undefined,
              probationMonth: values.probationMonth,
              probationSalaryRatio: values.probationSalaryRatio,
              remark: values.remark,
            };

            if (editingContract) {
              // 更新
              const updateReq: ContractUpdateRequest = dateValues;
              await updateContract(editingContract.id, updateReq);
              message.success('合同已更新');
            } else {
              // 新增
              const createReq: ContractCreateRequest = {
                ...dateValues,
                employeeId: selectedEmployeeId!,
                contractType: values.contractType!,
              };
              await createContract(createReq);
              message.success('合同已创建');
            }

            setEditVisible(false);
            setEditingContract(null);
            actionRef.current?.reload();
          } catch (err: any) {
            if (err?.errorFields) return; // 表单校验不通过
            message.error('操作失败');
          }
        }}
        width={560}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="contractNo" label="合同编号">
            <Input placeholder="系统自动生成" disabled={!!editingContract} />
          </Form.Item>
          <Form.Item
            name="contractType"
            label="合同类型"
            rules={[{ required: true, message: '请选择合同类型' }]}
          >
            <Select placeholder="请选择合同类型" allowClear>
              {Object.entries(CONTRACT_TYPE_MAP).map(([key, val]) => (
                <Option key={key} value={Number(key)}>
                  {val.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="startDate" label="合同开始日期">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="endDate"
            label="合同结束日期"
            dependencies={['startDate']}
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const start = getFieldValue('startDate');
                  if (start && value && !dayjs(value).isAfter(dayjs(start))) {
                    return Promise.reject(
                      new Error('结束日期必须晚于开始日期'),
                    );
                  }
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="probationMonth" label="试用期（月）">
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              max={12}
              placeholder="请输入试用期月数"
            />
          </Form.Item>
          <Form.Item name="probationSalaryRatio" label="试用期薪资比例（%）">
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              max={100}
              precision={2}
              placeholder="如 80.00"
            />
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

/**
 * 离职申请页面。对接离职申请分页和创建接口。
 */

import {
  getEmployeeDetail,
  getEmployeeList,
  type Employee,
  type EmployeeBrief,
} from '@/services/employee';
import { getDeptList } from '@/services/organization';
import type {
  LeaveApplication,
  LeaveApplicationCreateRequest,
} from '@/services/process';
import {
  ApprovalStatus,
  createLeaveApplication,
  getLeaveApplicationList,
  quickApproveLeaveApplication,
} from '@/services/process';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
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
import {
  Avatar,
  Button,
  Card,
  Form,
  Popconfirm,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatProcessDateTime } from '../utils';

const { Text } = Typography;

type SelectOption = {
  label: string;
  value: number;
};

type LeaveFormValues = LeaveApplicationCreateRequest & {
  employeeName?: string;
  departmentName?: string;
  positionName?: string;
};

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

/** 获取员工姓名首字，用于页面头像与卡片占位展示。 */
function getInitial(name?: string) {
  return name?.slice(0, 1) || '员';
}

/**
 * 离职申请页面组件。
 * 负责离职申请列表查询、员工信息回填、交接人选择和审批提交流程。
 */
const LeavePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [departmentOptions, setDepartmentOptions] = useState<SelectOption[]>(
    [],
  );
  const [handoverOptions, setHandoverOptions] = useState<SelectOption[]>([]);
  const [employeeLoading, setEmployeeLoading] = useState(false);
  const [handoverLoading, setHandoverLoading] = useState(false);
  const [handoverKeyword, setHandoverKeyword] = useState('');
  const [currentEmployeeDetail, setCurrentEmployeeDetail] =
    useState<Employee>();
  const [leaveForm] = Form.useForm<LeaveFormValues>();

  const watchedEmployeeId = Form.useWatch('employeeId', leaveForm);
  const watchedEmployeeName = Form.useWatch('employeeName', leaveForm);
  const watchedDepartmentName = Form.useWatch('departmentName', leaveForm);
  const watchedPositionName = Form.useWatch('positionName', leaveForm);

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
    void loadDepartments();
  }, []);

  const departmentFilterOption = useMemo(
    () => (input: string, option?: { label?: string | number }) =>
      String(option?.label || '')
        .toLowerCase()
        .includes(input.trim().toLowerCase()),
    [],
  );

  /** 重置员工关联字段，供员工不存在或抽屉关闭时统一清空表单上下文。 */
  const resetEmployeeRelatedFields = () => {
    leaveForm.setFieldsValue({
      employeeName: undefined,
      departmentName: undefined,
      positionName: undefined,
      handoverEmployeeId: undefined,
    });
    setCurrentEmployeeDetail(undefined);
    setHandoverOptions([]);
    setHandoverKeyword('');
  };

  /** 加载交接人选项，供同部门交接人下拉搜索复用。 */
  const loadHandoverOptions = async (keyword?: string) => {
    if (!currentEmployeeDetail?.deptId || !currentEmployeeDetail?.id) {
      setHandoverOptions([]);
      return;
    }
    setHandoverLoading(true);
    try {
      const page = await getEmployeeList({
        deptIds: [currentEmployeeDetail.deptId],
        keyword: keyword?.trim() || undefined,
        pageNum: 1,
        pageSize: 50,
      });
      const options = (page.records || [])
        .filter(
          (employee: EmployeeBrief) => employee.id !== currentEmployeeDetail.id,
        )
        .map((employee: EmployeeBrief) => ({
          label: `${employee.employeeName}（${employee.employeeNo} / ${employee.deptName}）`,
          value: employee.id,
        }));
      setHandoverOptions(options);
    } catch (error) {
      setHandoverOptions([]);
      message.error('工作交接人候选加载失败，请稍后重试');
    } finally {
      setHandoverLoading(false);
    }
  };

  /** 查询离职员工详情，内部调用 `loadHandoverOptions` 自动准备交接人候选列表。 */
  const handleEmployeeLookup = async (
    rawEmployeeId?: number | string | null,
  ) => {
    const employeeId = Number(rawEmployeeId);
    if (!employeeId || employeeId < 1) {
      resetEmployeeRelatedFields();
      return;
    }
    setEmployeeLoading(true);
    try {
      const detail = await getEmployeeDetail(employeeId);
      setCurrentEmployeeDetail(detail);
      leaveForm.setFieldsValue({
        employeeId: detail.id,
        employeeName: detail.employeeName,
        departmentName: detail.deptName,
        positionName: detail.postName,
        handoverEmployeeId: undefined,
      });
      setHandoverKeyword('');
      await loadHandoverOptions();
    } catch (error) {
      resetEmployeeRelatedFields();
      message.error('未找到该员工，请确认员工ID后重试');
    } finally {
      setEmployeeLoading(false);
    }
  };

  /** 触发员工信息查询，内部调用 `handleEmployeeLookup` 按输入的员工 ID 回填信息。 */
  const triggerEmployeeLookup = () => {
    void handleEmployeeLookup(leaveForm.getFieldValue('employeeId'));
  };

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
          <Avatar style={{ background: '#2f6fed' }}>
            {getInitial(record.employeeName)}
          </Avatar>
          <Space direction="vertical" size={0}>
            <strong>
              {record.employeeName || `员工 ${record.employeeId}`}
            </strong>
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
        leaveTypeOptions.find((item) => item.value === record.leaveType)
          ?.label ||
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
        const meta =
          statusMeta[record.approvalStatus ?? ApprovalStatus.DRAFT] ||
          statusMeta[0];
        return (
          <Tag color={meta.color}>{record.approvalStatusDesc || meta.text}</Tag>
        );
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
      width: 120,
      render: (_, record) =>
        record.approvalStatus === ApprovalStatus.APPROVING ? (
          <Popconfirm
            title="快速审批通过离职申请"
            description="确认后将直接完成当前离职审批流程。"
            onConfirm={async () => {
              await quickApproveLeaveApplication(record.id);
              message.success('已快速审批通过离职申请');
              actionRef.current?.reload();
            }}
          >
            <Button size="small" type="primary">
              快速审批
            </Button>
          </Popconfirm>
        ) : null,
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
                setDrawerOpen(true);
              }}
            >
              创建离职
            </Button>,
          ],
        }}
      />

      <DrawerForm<LeaveFormValues>
        form={leaveForm}
        title="离职申请表单"
        width={420}
        open={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            leaveForm.resetFields();
            resetEmployeeRelatedFields();
          }
        }}
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
        <Card size="small" title="员工信息" style={{ marginBottom: 16 }}>
          <Space>
            <Avatar size={40} style={{ background: '#2f6fed' }}>
              {getInitial(watchedEmployeeName)}
            </Avatar>
            <Space direction="vertical" size={0}>
              <strong>{watchedEmployeeName || '待选择员工'}</strong>
              <Text type="secondary">员工 ID：{watchedEmployeeId || '-'}</Text>
              <Text type="secondary">
                部门：{watchedDepartmentName || '-'} 职位：
                {watchedPositionName || '-'}
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
            rules={[{ required: true, message: '请输入离职员工ID' }]}
            fieldProps={{
              onBlur: () => {
                triggerEmployeeLookup();
              },
              onPressEnter: (event) => {
                event.preventDefault();
                triggerEmployeeLookup();
              },
            }}
          />
          <ProFormText
            name="employeeName"
            label="员工姓名"
            width="md"
            fieldProps={{
              readOnly: true,
              placeholder: employeeLoading
                ? '员工信息加载中...'
                : '输入员工ID后自动带出',
            }}
          />
          <ProFormText
            name="departmentName"
            label="当前部门"
            width="md"
            fieldProps={{
              readOnly: true,
              placeholder: employeeLoading
                ? '员工信息加载中...'
                : '输入员工ID后自动带出',
            }}
          />
          <ProFormText
            name="positionName"
            label="当前职位"
            width="md"
            fieldProps={{
              readOnly: true,
              placeholder: employeeLoading
                ? '员工信息加载中...'
                : '输入员工ID后自动带出',
            }}
          />
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
          fieldProps={{
            disabled: !currentEmployeeDetail?.deptId,
            loading: handoverLoading,
            showSearch: true,
            filterOption: false,
            allowClear: true,
            optionFilterProp: 'label',
            placeholder: currentEmployeeDetail?.deptId
              ? '请输入姓名或工号查询同部门人员'
              : '请先输入员工ID',
            onDropdownVisibleChange: (open) => {
              if (open && currentEmployeeDetail?.deptId) {
                void loadHandoverOptions(handoverKeyword);
              }
            },
            onSearch: (value) => {
              setHandoverKeyword(value);
              void loadHandoverOptions(value);
            },
          }}
        />
        <ProFormTextArea
          name="leaveReason"
          label="离职原因"
          fieldProps={{ rows: 4, maxLength: 500, showCount: true }}
          rules={[{ required: true, message: '请输入离职原因' }]}
        />
        <ProFormTextArea name="remark" label="备注" fieldProps={{ rows: 3 }} />
      </DrawerForm>
    </PageContainer>
  );
};

export default LeavePage;

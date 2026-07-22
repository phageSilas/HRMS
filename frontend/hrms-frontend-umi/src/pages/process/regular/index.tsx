/**
 * 转正管理页面。
 * 对接待转正列表和发起转正评估接口。
 */

import { getDeptList } from '@/services/organization';
import type {
  RegularApplication,
  RegularApplicationApplyRequest,
} from '@/services/process';
import {
  applyRegularApplication,
  getRegularApplicationList,
  quickApproveRegularApplication,
} from '@/services/process';
import { FileDoneOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  DrawerForm,
  PageContainer,
  ProFormDependency,
  ProFormDigit,
  ProFormRadio,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import {
  Avatar,
  Button,
  Popconfirm,
  Space,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatProcessDateTime } from '../utils';

const { Text } = Typography;

const resultOptions = [
  { label: '转正', value: 'pass' },
  { label: '延长试用', value: 'extend' },
  { label: '辞退', value: 'terminate' },
];

const statusMeta: Record<number, { text: string; color: string }> = {
  0: { text: '待审批', color: 'gold' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已通过', color: 'success' },
  3: { text: '已驳回', color: 'error' },
  4: { text: '已撤回', color: 'default' },
  5: { text: '已入职', color: 'blue' },
};

/** 获取员工姓名首字，用于列表头像展示。 */
function getInitial(name?: string) {
  return name?.slice(0, 1) || '员';
}

/** 渲染试用期剩余天数标签，用于快速识别超期与临近到期员工。 */
function renderRemainingDays(days?: number) {
  if (days === undefined || days === null) {
    return <Tag>未计算</Tag>;
  }
  if (days < 0) {
    return <Tag color="red">已超期 {Math.abs(days)} 天</Tag>;
  }
  if (days <= 7) {
    return <Tag color="gold">剩余 {days} 天</Tag>;
  }
  return <Tag color="green">剩余 {days} 天</Tag>;
}

/**
 * 转正管理页面组件。
 * 负责待转正员工查询、转正评估发起和已评估记录查看。
 */
const RegularPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [activeTab, setActiveTab] = useState<'pending' | 'evaluated'>(
    'pending',
  );
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [currentRow, setCurrentRow] = useState<RegularApplication>();
  const [departmentOptions, setDepartmentOptions] = useState<
    { label: string; value: number }[]
  >([]);

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
    () => (input: string, option?: { label?: string | number }) =>
      String(option?.label || '')
        .toLowerCase()
        .includes(input.trim().toLowerCase()),
    [],
  );

  const columns: ProColumns<RegularApplication>[] = [
    {
      title: '关键词',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: {
        placeholder: '员工姓名 / 工号',
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
      title: '员工',
      dataIndex: 'employeeName',
      width: 170,
      search: false,
      render: (_, record) => (
        <Space>
          <Avatar style={{ background: '#2f6fed' }}>
            {getInitial(record.employeeName)}
          </Avatar>
          <Space direction="vertical" size={0}>
            <strong>{record.employeeName}</strong>
            <Text type="secondary">
              {record.employeeNo || `ID ${record.employeeId}`}
            </Text>
          </Space>
        </Space>
      ),
    },
    { title: '部门', dataIndex: 'departmentName', width: 120, search: false },
    { title: '职位', dataIndex: 'positionName', width: 160, search: false },
    {
      title: '入职日期',
      dataIndex: 'hireDate',
      valueType: 'date',
      width: 120,
      search: false,
    },
    {
      title: '试用期结束',
      dataIndex: 'probationEndDate',
      valueType: 'date',
      width: 130,
      search: false,
    },
    {
      title: '剩余天数',
      dataIndex: 'remainingDays',
      width: 120,
      search: false,
      render: (_, record) => renderRemainingDays(record.remainingDays),
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 120,
      search: false,
      render: (_, record) => {
        const meta = statusMeta[record.approvalStatus ?? 0] || {
          text: record.approvalStatusDesc || '待审批',
          color: 'default',
        };
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
      width: 140,
      render: (_, record) => {
        if (record.approvalStatus === 1 && record.id) {
          return (
            <Popconfirm
              title="快速审批通过转正申请"
              description="确认后将直接完成当前转正审批流程。"
              onConfirm={async () => {
                await quickApproveRegularApplication(record.id!);
                message.success('已快速审批通过转正申请');
                actionRef.current?.reload();
              }}
            >
              <Button size="small" type="primary">
                快速审批
              </Button>
            </Popconfirm>
          );
        }
        const disabled = activeTab !== 'pending';
        const buttonText = record.approvalStatus === 1 ? '审批中' : '发起转正';
        return (
          <Button
            size="small"
            type={activeTab === 'pending' ? 'primary' : 'default'}
            icon={<FileDoneOutlined />}
            disabled={disabled}
            onClick={() => {
              if (disabled) {
                return;
              }
              setCurrentRow(record);
              setDrawerOpen(true);
            }}
          >
            {buttonText}
          </Button>
        );
      },
    },
  ];

  return (
    <PageContainer
      header={{
        title: '转正管理',
        subTitle: '试用期到期提醒、转正评估与审批发起',
      }}
    >
      <Tabs
        activeKey={activeTab}
        onChange={(key) => {
          setActiveTab(key as 'pending' | 'evaluated');
          setTimeout(() => actionRef.current?.reload(), 0);
        }}
        items={[
          { key: 'pending', label: '待转正' },
          { key: 'evaluated', label: '已评估' },
        ]}
      />

      <ProTable<RegularApplication>
        actionRef={actionRef}
        rowKey={(record) =>
          `${activeTab}-${record.employeeId}-${record.id || 'pending'}`
        }
        columns={columns}
        request={async (params) => {
          const result = await getRegularApplicationList({
            tab: activeTab,
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword: params.keyword as string,
            departmentId: params.departmentId as number,
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
          title: activeTab === 'pending' ? '待转正员工' : '已评估记录',
        }}
      />

      <DrawerForm<RegularApplicationApplyRequest>
        title="发起转正评估"
        width={640}
        open={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            setCurrentRow(undefined);
          }
        }}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        initialValues={{ result: 'pass' }}
        onFinish={async (values) => {
          if (!currentRow) {
            return false;
          }
          await applyRegularApplication(currentRow.employeeId, values);
          message.success('已发起转正评估');
          setDrawerOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <Space direction="vertical" size={4} style={{ marginBottom: 16 }}>
          <Space>
            <Avatar style={{ background: '#2f6fed' }}>
              {getInitial(currentRow?.employeeName)}
            </Avatar>
            <Space direction="vertical" size={0}>
              <strong>{currentRow?.employeeName}</strong>
              <Text type="secondary">
                {currentRow?.departmentName || '-'} /{' '}
                {currentRow?.positionName || '-'} / 入职：
                {currentRow?.hireDate || '-'}
              </Text>
            </Space>
          </Space>
        </Space>
        <ProFormTextArea
          name="evaluateOpinion"
          label="表现评价"
          rules={[{ required: true, message: '请输入表现评价' }]}
          fieldProps={{ rows: 4 }}
        />
        <ProFormRadio.Group
          name="result"
          label="评估结果"
          options={resultOptions}
          rules={[{ required: true, message: '请选择评估结果' }]}
        />
        <ProFormDependency name={['result']}>
          {({ result }) => (
            <>
              {result !== 'terminate' && (
                <ProFormDigit
                  name="salaryAdjustment"
                  label="转正后薪资调整"
                  min={0}
                  width="md"
                  fieldProps={{ precision: 2 }}
                />
              )}
              {result === 'extend' && (
                <ProFormDigit
                  name="extendMonth"
                  label="延长试用月数"
                  min={1}
                  max={6}
                  width="sm"
                  rules={[{ required: true, message: '请输入延长试用月数' }]}
                />
              )}
            </>
          )}
        </ProFormDependency>
      </DrawerForm>
    </PageContainer>
  );
};

export default RegularPage;

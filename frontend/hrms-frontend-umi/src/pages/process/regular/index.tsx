/**
 * 转正管理页面。
 * 接入待转正列表和发起转正评估接口。
 */

import {
  applyRegularApplication,
  getRegularApplicationList,
} from '@/services/process';
import type {
  RegularApplication,
  RegularApplicationApplyRequest,
} from '@/services/process';
import { FileDoneOutlined } from '@ant-design/icons';
import {
  DrawerForm,
  PageContainer,
  ProFormDependency,
  ProFormDigit,
  ProFormRadio,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Space, Tabs, Tag, message } from 'antd';
import React, { useRef, useState } from 'react';

const departmentOptions = [
  { label: '人力资源部', value: 1 },
  { label: '技术部', value: 2 },
  { label: '产品部', value: 3 },
  { label: '财务部', value: 4 },
];

const resultOptions = [
  { label: '转正', value: 'pass' },
  { label: '延长试用', value: 'extend' },
  { label: '辞退', value: 'terminate' },
];

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

const RegularPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [activeTab, setActiveTab] = useState<'pending' | 'evaluated'>('pending');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [currentRow, setCurrentRow] = useState<RegularApplication>();

  const columns: ProColumns<RegularApplication>[] = [
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
      title: '员工',
      dataIndex: 'employeeName',
      width: 150,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <strong>{record.employeeName}</strong>
          <span style={{ color: '#6b7280', fontSize: 12 }}>
            {record.employeeNo || `ID ${record.employeeId}`}
          </span>
        </Space>
      ),
    },
    {
      title: '部门',
      dataIndex: 'departmentName',
      width: 120,
      search: false,
    },
    {
      title: '职位',
      dataIndex: 'positionName',
      width: 160,
      search: false,
    },
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
        if (activeTab === 'pending') {
          return <Tag color="gold">待转正</Tag>;
        }
        return (
          <Tag color={record.approvalStatus === 2 ? 'success' : 'processing'}>
            {record.approvalStatusDesc || '已评估'}
          </Tag>
        );
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
      width: 140,
      render: (_, record) => (
        <Button
          size="small"
          type={activeTab === 'pending' ? 'primary' : 'default'}
          icon={<FileDoneOutlined />}
          disabled={activeTab !== 'pending'}
          onClick={() => {
            setCurrentRow(record);
            setDrawerOpen(true);
          }}
        >
          发起转正
        </Button>
      ),
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
        rowKey={(record) => `${activeTab}-${record.employeeId}-${record.id || 'pending'}`}
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
        toolbar={{ title: activeTab === 'pending' ? '待转正员工' : '已评估记录' }}
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
          <strong>{currentRow?.employeeName}</strong>
          <span style={{ color: '#6b7280' }}>
            {currentRow?.departmentName || '-'} / {currentRow?.positionName || '-'} / 入职：
            {currentRow?.hireDate || '-'}
          </span>
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

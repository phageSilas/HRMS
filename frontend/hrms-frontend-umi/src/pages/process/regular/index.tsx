/**
 * 转正管理页面。
 * 当前提供待转正/已评估基础界面，后续接入 /api/v1/regular-applications。
 */

import {
  DrawerForm,
  PageContainer,
  ProFormDigit,
  ProFormRadio,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { FileDoneOutlined } from '@ant-design/icons';
import { Button, Space, Tabs, Tag, message } from 'antd';
import React, { useMemo, useState } from 'react';

interface RegularRow {
  id: number;
  employeeName: string;
  departmentName: string;
  positionName: string;
  hireDate: string;
  probationEndDate: string;
  remainingDays: number;
  status: 'pending' | 'evaluated';
  result?: string;
}

const sampleRows: RegularRow[] = [
  {
    id: 1,
    employeeName: '张晓雨',
    departmentName: '技术部',
    positionName: '前端开发工程师',
    hireDate: '2026-04-15',
    probationEndDate: '2026-07-15',
    remainingDays: 1,
    status: 'pending',
  },
  {
    id: 2,
    employeeName: '陈辰',
    departmentName: '产品部',
    positionName: '产品经理',
    hireDate: '2026-03-20',
    probationEndDate: '2026-06-20',
    remainingDays: -24,
    status: 'evaluated',
    result: '转正',
  },
];

const RegularPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('pending');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [currentRow, setCurrentRow] = useState<RegularRow>();

  const rows = useMemo(
    () => sampleRows.filter((item) => item.status === activeTab),
    [activeTab],
  );

  const columns: ProColumns<RegularRow>[] = [
    { title: '员工姓名', dataIndex: 'employeeName', width: 120 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    { title: '职位', dataIndex: 'positionName', width: 160 },
    { title: '入职日期', dataIndex: 'hireDate', valueType: 'date', width: 120 },
    {
      title: '试用期结束',
      dataIndex: 'probationEndDate',
      valueType: 'date',
      width: 130,
    },
    {
      title: '剩余天数',
      dataIndex: 'remainingDays',
      width: 110,
      render: (_, record) => {
        if (record.remainingDays < 0) {
          return <Tag color="red">已超期 {Math.abs(record.remainingDays)} 天</Tag>;
        }
        if (record.remainingDays <= 7) {
          return <Tag color="gold">剩余 {record.remainingDays} 天</Tag>;
        }
        return <Tag color="green">剩余 {record.remainingDays} 天</Tag>;
      },
    },
    {
      title: '评估结果',
      dataIndex: 'result',
      width: 110,
      renderText: (value) => value || '待评估',
    },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, record) => (
        <Button
          size="small"
          type={record.status === 'pending' ? 'primary' : 'default'}
          icon={<FileDoneOutlined />}
          onClick={() => {
            setCurrentRow(record);
            setDrawerOpen(true);
          }}
        >
          {record.status === 'pending' ? '发起转正' : '查看评估'}
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
        onChange={setActiveTab}
        items={[
          { key: 'pending', label: '待转正' },
          { key: 'evaluated', label: '已评估' },
        ]}
      />
      <ProTable<RegularRow>
        rowKey="id"
        search={false}
        columns={columns}
        dataSource={rows}
        pagination={false}
        toolBarRender={() => [
          <Button key="scan" disabled>
            每日扫描待转正员工
          </Button>,
        ]}
      />

      <DrawerForm
        title="转正评估"
        width={640}
        open={drawerOpen}
        onOpenChange={setDrawerOpen}
        drawerProps={{ destroyOnClose: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        onFinish={async () => {
          message.info('转正接口尚未接入，已保留提交审批入口');
          setDrawerOpen(false);
          return true;
        }}
      >
        <Space direction="vertical" size={4} style={{ marginBottom: 16 }}>
          <strong>{currentRow?.employeeName}</strong>
          <span style={{ color: '#6b7280' }}>
            {currentRow?.departmentName} / {currentRow?.positionName} / 入职：
            {currentRow?.hireDate}
          </span>
        </Space>
        <ProFormTextArea
          name="evaluation"
          label="表现评价"
          rules={[{ required: true, message: '请输入表现评价' }]}
          fieldProps={{ rows: 4 }}
        />
        <ProFormRadio.Group
          name="result"
          label="评估结果"
          initialValue="pass"
          options={[
            { label: '转正', value: 'pass' },
            { label: '延长试用', value: 'extend' },
            { label: '辞退', value: 'terminate' },
          ]}
        />
        <ProFormDigit name="newSalary" label="转正后薪资" min={0} width="md" />
        <ProFormDigit name="extendMonths" label="延长试用月数" min={1} max={6} width="sm" />
      </DrawerForm>
    </PageContainer>
  );
};

export default RegularPage;

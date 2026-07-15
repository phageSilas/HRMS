/**
 * 我的请假页面
 * 假期余额 + 请假记录列表 + 提交/取消请假
 */

import { PlusOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  message,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Statistic,
  Table,
  Tabs,
  Tag,
  Typography,
} from 'antd';
import dayjs from 'dayjs';
import React, { useMemo, useState } from 'react';
import { cancelLeave, createLeave, getLeaveBalance, getLeaveList } from '@/services/profile';
import type { LeaveRequestDTO } from '@/services/profile';

const { Text } = Typography;

// ============ 常量 ============

const LEAVE_TYPE_OPTIONS = [
  { label: '年假', value: 'ANNUAL' },
  { label: '调休', value: 'COMPASSIONATE' },
  { label: '病假', value: 'SICK' },
  { label: '事假', value: 'PERSONAL' },
  { label: '婚假', value: 'MARRIAGE' },
  { label: '产假', value: 'MATERNITY' },
  { label: '丧假', value: 'FUNERAL' },
];

const STATUS_TAB_MAP: Record<string, number[] | undefined> = {
  all: undefined,
  pending: [0, 1],
  approved: [2],
  rejected: [3],
};

const STATUS_TAG_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已通过', color: 'success' },
  3: { text: '已拒绝', color: 'error' },
  4: { text: '已撤回', color: 'warning' },
};

// ============ 页面组件 ============

const ProfileLeavePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('all');
  const [submitModalOpen, setSubmitModalOpen] = useState(false);
  const [submitForm] = Form.useForm();

  // 假期余额
  const { data: balanceData, loading: balanceLoading } = useRequest(getLeaveBalance);

  // 请假记录
  const { data: leaveData, loading: leaveLoading, refresh: refreshLeaves } = useRequest(getLeaveList);

  const balance = balanceData;
  const allLeaves = leaveData || [];

  // ============ 按 Tab 过滤 ============

  const filteredLeaves = useMemo(() => {
    const statuses = STATUS_TAB_MAP[activeTab];
    if (!statuses) return allLeaves;
    return allLeaves.filter((l) => statuses.includes(l.approvalStatus));
  }, [allLeaves, activeTab]);

  // ============ 提交请假 ============

  const handleSubmitLeave = async () => {
    try {
      const values = await submitForm.validateFields();
      const startTime = values.dateRange[0];
      const endTime = values.dateRange[1];
      const totalDays = endTime.diff(startTime, 'day') + 1;

      const payload: LeaveRequestDTO = {
        leaveType: values.leaveType,
        startTime: startTime.format('YYYY-MM-DDTHH:mm:ss'),
        endTime: endTime.format('YYYY-MM-DDTHH:mm:ss'),
        totalDays,
        leaveReason: values.leaveReason,
        attachmentUrl: undefined,
      };
      await createLeave(payload);
      message.success('请假申请已提交');
      setSubmitModalOpen(false);
      submitForm.resetFields();
      refreshLeaves();
    } catch {
      // 静默处理
    }
  };

  // ============ 取消请假 ============

  const handleCancelLeave = (id: number) => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消该请假申请吗？取消后不可恢复。',
      okText: '确认取消',
      cancelText: '再想想',
      onOk: async () => {
        try {
          await cancelLeave(id);
          message.success('请假已取消');
          refreshLeaves();
        } catch {
          // 静默处理
        }
      },
    });
  };

  // ============ 记录表格列 ============

  const leaveColumns = [
    { title: '请假类型', dataIndex: 'leaveTypeDesc', key: 'leaveTypeDesc', width: 100 },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 170,
      render: (t: string) => t || '-',
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      width: 170,
      render: (t: string) => t || '-',
    },
    { title: '天数', dataIndex: 'totalDays', key: 'totalDays', width: 60 },
    { title: '事由', dataIndex: 'leaveReason', key: 'leaveReason', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 100,
      render: (s: number) => {
        const item = STATUS_TAG_MAP[s] || { text: '未知', color: 'default' };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: any, record: { id: number; approvalStatus: number }) => {
        // 草稿(0)或审批中(1)可取消
        if (record.approvalStatus === 0 || record.approvalStatus === 1) {
          return (
            <Button type="link" danger onClick={() => handleCancelLeave(record.id)}>
              取消
            </Button>
          );
        }
        return '-';
      },
    },
  ];

  // ============ 渲染 ============

  return (
    <PageContainer>
      {/* 假期余额卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12}>
          <Card bordered={false} style={{ borderRadius: 8 }}>
            {balanceLoading ? (
              <Spin />
            ) : (
              <Statistic
                title="年假余额"
                value={balance?.annualRemaining ?? '-'}
                suffix={
                  <Text type="secondary">
                    / {balance?.annualTotal ?? '-'} 天
                  </Text>
                }
                valueStyle={{ color: '#1677ff' }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card bordered={false} style={{ borderRadius: 8 }}>
            {balanceLoading ? (
              <Spin />
            ) : (
              <Statistic
                title="调休余额"
                value={balance?.compassionateRemaining ?? '-'}
                suffix={
                  <Text type="secondary">
                    / {balance?.compassionateTotal ?? '-'} 小时
                  </Text>
                }
                valueStyle={{ color: '#52c41a' }}
              />
            )}
          </Card>
        </Col>
      </Row>

      {/* 请假记录 */}
      <Card
        bordered={false}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setSubmitModalOpen(true)}>
            提交请假
          </Button>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'all', label: '全部' },
            { key: 'pending', label: '审批中' },
            { key: 'approved', label: '已通过' },
            { key: 'rejected', label: '已拒绝' },
          ]}
        />
        <Table
          dataSource={filteredLeaves}
          columns={leaveColumns}
          rowKey="id"
          loading={leaveLoading}
          pagination={false}
          locale={{ emptyText: '暂无请假记录' }}
        />
      </Card>

      {/* 提交请假弹窗 */}
      <Modal
        title="提交请假申请"
        open={submitModalOpen}
        onOk={handleSubmitLeave}
        onCancel={() => {
          setSubmitModalOpen(false);
          submitForm.resetFields();
        }}
        width={560}
        destroyOnClose
      >
        <Form form={submitForm} layout="vertical">
          <Form.Item
            name="leaveType"
            label="请假类型"
            rules={[{ required: true, message: '请选择请假类型' }]}
          >
            <Select placeholder="请选择请假类型">
              {LEAVE_TYPE_OPTIONS.map((opt) => (
                <Select.Option key={opt.value} value={opt.value}>
                  {opt.label}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="dateRange"
            label="请假时间"
            rules={[{ required: true, message: '请选择请假时间范围' }]}
          >
            <DatePicker.RangePicker
              showTime
              style={{ width: '100%' }}
              format="YYYY-MM-DD HH:mm"
            />
          </Form.Item>
          <Form.Item
            name="leaveReason"
            label="请假事由"
            rules={[{ required: true, message: '请输入请假事由' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入请假事由" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ProfileLeavePage;

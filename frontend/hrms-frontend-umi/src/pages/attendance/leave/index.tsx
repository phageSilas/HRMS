import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type { AttendanceLeaveType } from '@/services/attendance';
import { getAttendanceLeaveTypes } from '@/services/attendance';
import {
  cancelLeave,
  createLeave,
  getLeaveBalance,
  getLeaveList,
} from '@/services/profile';
import type {
  LeaveBalanceVO,
  LeaveRequestDTO,
  LeaveVO,
} from '@/services/profile';
import {
  ClockCircleOutlined,
  FileSearchOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Progress,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Paragraph, Text, Title } = Typography;

const STATUS_TAB_MAP: Record<string, number[] | undefined> = {
  all: undefined,
  pending: [0, 1],
  approved: [2],
  rejected: [3],
};

const STATUS_TAG_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已批准', color: 'success' },
  3: { text: '已拒绝', color: 'error' },
  4: { text: '已撤回', color: 'warning' },
};

const APPROVAL_RULES = [
  { leaveType: '年假/调休 ≤ 3天', approver: '直接上级' },
  { leaveType: '年假/调休 > 3天', approver: '直接上级 → 部门负责人' },
  { leaveType: '病假/事假 ≤ 1天', approver: '直接上级' },
  { leaveType: '病假/事假 > 1天', approver: '直接上级 → 部门负责人' },
  { leaveType: '婚假/产假/丧假', approver: '直接上级 → HR备案' },
];

function formatDateTime(value?: string) {
  if (!value) {
    return '--';
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value;
}

function formatDateRange(startTime?: string, endTime?: string) {
  return `${formatDateTime(startTime)} - ${formatDateTime(endTime)}`;
}

function buildBalanceCards(balance?: LeaveBalanceVO) {
  return [
    {
      key: 'annual',
      title: '年假余额',
      value: balance?.annualRemaining ?? 0,
      total: balance?.annualTotal ?? 0,
      strokeColor: '#1677ff',
      unit: '天',
    },
    {
      key: 'compassionate',
      title: '调休余额',
      value: balance?.compassionateRemaining ?? 0,
      total: balance?.compassionateTotal ?? 0,
      strokeColor: '#722ed1',
      unit: '天',
    },
  ];
}

const AttendanceLeavePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('all');
  const [submitModalOpen, setSubmitModalOpen] = useState(false);
  const [submitForm] = Form.useForm();
  const [balance, setBalance] = useState<LeaveBalanceVO>();
  const [leaveList, setLeaveList] = useState<LeaveVO[]>([]);
  const [leaveTypes, setLeaveTypes] = useState<AttendanceLeaveType[]>([]);
  const [loading, setLoading] = useState(false);
  const [leaveTypeLoading, setLeaveTypeLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [cancellingId, setCancellingId] = useState<number>();

  const loadLeavePageData = async () => {
    setLoading(true);
    try {
      const [balanceData, leaveData] = await Promise.all([getLeaveBalance(), getLeaveList()]);
      setBalance(balanceData);
      setLeaveList(leaveData || []);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '我的请假数据加载失败';
      message.error(messageText);
    } finally {
      setLoading(false);
    }
  };

  const loadLeaveTypes = async () => {
    setLeaveTypeLoading(true);
    try {
      const nextLeaveTypes = await getAttendanceLeaveTypes();
      setLeaveTypes(nextLeaveTypes || []);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '请假类型加载失败';
      message.error(messageText);
    } finally {
      setLeaveTypeLoading(false);
    }
  };

  useEffect(() => {
    void loadLeavePageData();
    void loadLeaveTypes();
  }, []);

  usePageAutoRefresh(() => {
    void loadLeavePageData();
  });

  const filteredLeaves = useMemo(() => {
    const statuses = STATUS_TAB_MAP[activeTab];
    if (!statuses) {
      return leaveList;
    }
    return leaveList.filter((item) => statuses.includes(item.approvalStatus));
  }, [activeTab, leaveList]);

  const ruleColumns: ColumnsType<(typeof APPROVAL_RULES)[number]> = [
    {
      title: '请假类型 + 天数',
      dataIndex: 'leaveType',
      key: 'leaveType',
    },
    {
      title: '审批人',
      dataIndex: 'approver',
      key: 'approver',
    },
  ];

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

      setSubmitting(true);
      await createLeave(payload);
      message.success('请假申请已提交');
      setSubmitModalOpen(false);
      submitForm.resetFields();
      await loadLeavePageData();
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelLeave = (record: LeaveVO) => {
    Modal.confirm({
      title: '确认取消请假申请',
      content: '取消后不可恢复，审批中的流程也会一并终止。',
      okText: '确认取消',
      cancelText: '再想想',
      onOk: async () => {
        try {
          setCancellingId(record.id);
          await cancelLeave(record.id);
          message.success('请假申请已取消');
          await loadLeavePageData();
        } catch (error) {
          const messageText = error instanceof Error ? error.message : '取消请假失败';
          message.error(messageText);
        } finally {
          setCancellingId(undefined);
        }
      },
    });
  };

  const balanceCards = buildBalanceCards(balance);

  return (
    <PageContainer
      title={false}
      content={
        <Row justify="space-between" align="middle" gutter={[16, 16]}>
          <Col>
            <Space direction="vertical" size={4}>
              <Title level={2} style={{ margin: 0 }}>
                请假管理
              </Title>
              <Text type="secondary">查看余额、申请请假、跟踪审批进度</Text>
            </Space>
          </Col>
          <Col>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setSubmitModalOpen(true)}
            >
              申请请假
            </Button>
          </Col>
        </Row>
      }
    >
      <Spin spinning={loading}>
        <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
          <Space direction="vertical" size={20} style={{ width: '100%' }}>
            <Text strong>假期余额</Text>
            <Row gutter={[24, 24]} justify="center">
              {balanceCards.map((item) => {
                const percent = item.total > 0 ? Math.min((item.value / item.total) * 100, 100) : 0;
                return (
                  <Col xs={24} sm={12} md={8} key={item.key}>
                    <Card
                      bordered={false}
                      style={{
                        borderRadius: 18,
                        background: '#fafafa',
                        textAlign: 'center',
                      }}
                    >
                      <Progress
                        type="dashboard"
                        percent={Number(percent.toFixed(0))}
                        strokeColor={item.strokeColor}
                        trailColor="#f0f0f0"
                        format={() => `${item.value}`}
                      />
                      <Space direction="vertical" size={2}>
                        <Text strong>{item.title}</Text>
                        <Text type="secondary">
                          共 {item.total} {item.unit}
                        </Text>
                      </Space>
                    </Card>
                  </Col>
                );
              })}
            </Row>
          </Space>
        </Card>

        <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Text strong>审批流规则</Text>
            <Table
              rowKey="leaveType"
              columns={ruleColumns}
              dataSource={APPROVAL_RULES}
              pagination={false}
              size="small"
            />
          </Space>
        </Card>

        <Card bordered={false} style={{ borderRadius: 20 }}>
          <Space direction="vertical" size={20} style={{ width: '100%' }}>
            <Space align="center">
              <Text strong>请假记录</Text>
              <Button
                type={activeTab === 'all' ? 'primary' : 'default'}
                size="small"
                onClick={() => setActiveTab('all')}
              >
                全部
              </Button>
              <Button
                type={activeTab === 'pending' ? 'primary' : 'default'}
                size="small"
                onClick={() => setActiveTab('pending')}
              >
                审批中
              </Button>
              <Button
                type={activeTab === 'approved' ? 'primary' : 'default'}
                size="small"
                onClick={() => setActiveTab('approved')}
              >
                已通过
              </Button>
              <Button
                type={activeTab === 'rejected' ? 'primary' : 'default'}
                size="small"
                onClick={() => setActiveTab('rejected')}
              >
                已拒绝
              </Button>
            </Space>

            {filteredLeaves.length > 0 ? (
              <List
                itemLayout="vertical"
                dataSource={filteredLeaves}
                renderItem={(item) => {
                  const statusMeta = STATUS_TAG_MAP[item.approvalStatus] || STATUS_TAG_MAP[0];
                  return (
                    <List.Item key={item.id} style={{ padding: 0, border: 'none' }}>
                      <Card
                        bordered={false}
                        style={{
                          borderRadius: 16,
                          background: '#fafafa',
                          marginBottom: 12,
                        }}
                        bodyStyle={{ padding: 20 }}
                      >
                        <Row gutter={[16, 16]} justify="space-between" align="middle">
                          <Col flex="auto">
                            <Space direction="vertical" size={10} style={{ width: '100%' }}>
                              <Space wrap>
                                <Text strong style={{ fontSize: 16 }}>
                                  {item.leaveTypeDesc || item.leaveType}
                                </Text>
                                <Tag color={statusMeta.color}>{item.approvalStatusDesc || statusMeta.text}</Tag>
                                <Text type="secondary">{item.totalDays} 天</Text>
                              </Space>
                              <Text>{formatDateRange(item.startTime, item.endTime)}</Text>
                              <Paragraph style={{ marginBottom: 0 }}>
                                {item.leaveReason || '暂无请假事由'}
                              </Paragraph>
                              <Space split={<Text type="secondary">|</Text>} wrap>
                                <Text type="secondary">
                                  申请时间：{formatDateTime(item.createTime)}
                                </Text>
                                {item.approvalInstanceId ? (
                                  <Text type="secondary">审批流程已生成</Text>
                                ) : (
                                  <Text type="secondary">等待流程生成</Text>
                                )}
                              </Space>
                            </Space>
                          </Col>
                          <Col>
                            <Space direction="vertical">
                              {item.approvalInstanceId ? (
                                <Button
                                  type="link"
                                  icon={<FileSearchOutlined />}
                                  onClick={() =>
                                    history.push(`/approval/detail/${item.approvalInstanceId}`)
                                  }
                                >
                                  查看进度
                                </Button>
                              ) : null}
                              {item.approvalStatus === 0 || item.approvalStatus === 1 ? (
                                <Button
                                  danger
                                  loading={cancellingId === item.id}
                                  onClick={() => handleCancelLeave(item)}
                                >
                                  撤销
                                </Button>
                              ) : null}
                            </Space>
                          </Col>
                        </Row>
                      </Card>
                    </List.Item>
                  );
                }}
              />
            ) : (
              <Empty description="暂无请假记录" />
            )}
          </Space>
        </Card>
      </Spin>

      <Modal
        title="申请请假"
        open={submitModalOpen}
        onOk={handleSubmitLeave}
        onCancel={() => {
          setSubmitModalOpen(false);
          submitForm.resetFields();
        }}
        confirmLoading={submitting}
        width={640}
        destroyOnClose
      >
        <Form form={submitForm} layout="vertical">
          <Form.Item
            name="leaveType"
            label="请假类型"
            rules={[{ required: true, message: '请选择请假类型' }]}
          >
            <Select
              placeholder="请选择请假类型"
              loading={leaveTypeLoading}
              options={leaveTypes.map((item) => ({
                label: item.label,
                value: item.value,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="dateRange"
            label="请假时间"
            rules={[{ required: true, message: '请选择请假时间范围' }]}
          >
            <DatePicker.RangePicker
              showTime
              format="YYYY-MM-DD HH:mm"
              style={{ width: '100%' }}
              disabledDate={(current) => current != null && current < dayjs().startOf('day')}
            />
          </Form.Item>
          <Form.Item
            name="leaveReason"
            label="请假事由"
            rules={[{ required: true, message: '请输入请假事由' }]}
          >
            <Input.TextArea
              rows={4}
              maxLength={200}
              showCount
              placeholder="请简要说明请假原因，方便审批人快速了解背景。"
            />
          </Form.Item>
          <Card
            size="small"
            bordered={false}
            style={{ borderRadius: 14, background: '#fafafa' }}
          >
            <Space align="start">
              <ClockCircleOutlined style={{ color: '#1677ff', marginTop: 4 }} />
              <Text type="secondary">
                提交后会自动进入审批流程。审批中的申请可撤销，已通过或已拒绝的记录请通过“查看进度”了解详情。
              </Text>
            </Space>
          </Card>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default AttendanceLeavePage;

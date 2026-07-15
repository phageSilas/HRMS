/**
 * 我的考勤页面
 * 考勤日历月视图 + 打卡 + 补卡申请
 */

import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Descriptions,
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
  Tag,
  Typography,
} from 'antd';
import dayjs from 'dayjs';
import React, { useMemo, useState } from 'react';
import {
  clockIn,
  createMakeup,
  getAttendanceCalendar,
  getMakeupRecords,
} from '@/services/profile';
import type { MakeupRequest } from '@/services/profile';

const { Text, Title } = Typography;

// ============ 考勤状态颜色映射（与后端 AttendanceServiceImpl 对齐） ============

const STATUS_COLOR_MAP: Record<string, string> = {
  NORMAL: '#52c41a',
  LATE: '#fa8c16',
  EARLY_LEAVE: '#faad14',
  MISSED: '#ff4d4f',
  LEAVE: '#1677ff',
  HOLIDAY: '#d9d9d9',
  ABSENT: '#cf1322',
};

const STATUS_BG_MAP: Record<string, string> = {
  NORMAL: '#f6ffed',
  LATE: '#fff7e6',
  EARLY_LEAVE: '#fffbe6',
  MISSED: '#fff2f0',
  LEAVE: '#e6f4ff',
  HOLIDAY: '#fafafa',
  ABSENT: '#fff1f0',
};

// ============ 页面组件 ============

const ProfileAttendancePage: React.FC = () => {
  const [currentMonth, setCurrentMonth] = useState(dayjs().format('YYYY-MM'));
  const [makeupModalOpen, setMakeupModalOpen] = useState(false);
  const [makeupForm] = Form.useForm();

  // 考勤日历
  const { data: calendarData, loading: calendarLoading, refresh: refreshCalendar } = useRequest(
    () => getAttendanceCalendar(currentMonth),
  );

  // 补卡记录
  const { data: makeupData, loading: makeupLoading, refresh: refreshMakeup } = useRequest(
    getMakeupRecords,
  );

  const calendar = calendarData?.data;
  const makeupRecords = makeupData?.data || [];

  // ============ 打卡 ============

  const handleClockIn = async (type: number) => {
    try {
      await clockIn({ type });
      message.success(type === 1 ? '上班打卡成功' : '下班打卡成功');
      refreshCalendar();
    } catch {
      // 错误由 request 拦截器统一处理
    }
  };

  // ============ 补卡申请 ============

  const handleMakeupSubmit = async () => {
    try {
      const values = await makeupForm.validateFields();
      const payload: MakeupRequest = {
        correctionDate: values.correctionDate.format('YYYY-MM-DD'),
        correctionType: values.correctionType,
        correctionReason: values.correctionReason,
      };
      await createMakeup(payload);
      message.success('补卡申请已提交');
      setMakeupModalOpen(false);
      makeupForm.resetFields();
      refreshMakeup();
      refreshCalendar();
    } catch {
      // 静默处理
    }
  };

  // ============ 日历渲染 ============

  const calendarDays = useMemo(() => {
    if (!calendar?.days) return [];
    return calendar.days;
  }, [calendar]);

  // 统计
  const statistics = useMemo(() => {
    const stats: Record<string, number> = {
      NORMAL: 0,
      LATE: 0,
      EARLY_LEAVE: 0,
      MISSED: 0,
      LEAVE: 0,
      HOLIDAY: 0,
      ABSENT: 0,
    };
    calendarDays.forEach((day) => {
      if (stats[day.status] !== undefined) stats[day.status]++;
    });
    return stats;
  }, [calendarDays]);

  // ============ 补卡记录表格列 ============

  const makeupColumns = [
    { title: '补卡日期', dataIndex: 'correctionDate', key: 'correctionDate', width: 120 },
    {
      title: '补卡类型',
      dataIndex: 'correctionType',
      key: 'correctionType',
      width: 100,
      render: (t: string) => (t === 'CLOCK_IN' ? '上班卡' : '下班卡'),
    },
    { title: '原因', dataIndex: 'correctionReason', key: 'correctionReason', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 100,
      render: (s: number) => {
        const map: Record<number, { text: string; color: string }> = {
          0: { text: '草稿', color: 'default' },
          1: { text: '审批中', color: 'processing' },
          2: { text: '已通过', color: 'success' },
          3: { text: '已拒绝', color: 'error' },
          4: { text: '已撤回', color: 'warning' },
        };
        const item = map[s] || { text: '未知', color: 'default' };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (t: string) => t || '-',
    },
  ];

  // ============ 渲染 ============

  return (
    <PageContainer>
      {/* 月度统计和打卡入口 */}
      <Card bordered={false} style={{ marginBottom: 16 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col>
            <DatePicker
              picker="month"
              value={dayjs(currentMonth, 'YYYY-MM')}
              onChange={(d) => d && setCurrentMonth(d.format('YYYY-MM'))}
              allowClear={false}
            />
          </Col>
          <Col flex="auto">
            <Space size="large">
              <Statistic
                title="出勤"
                value={statistics.NORMAL}
                valueStyle={{ color: '#52c41a', fontSize: 20 }}
                suffix="天"
              />
              <Statistic
                title="迟到"
                value={statistics.LATE}
                valueStyle={{ color: '#fa8c16', fontSize: 20 }}
                suffix="次"
              />
              <Statistic
                title="早退"
                value={statistics.EARLY_LEAVE}
                valueStyle={{ color: '#faad14', fontSize: 20 }}
                suffix="次"
              />
              <Statistic
                title="缺卡"
                value={statistics.MISSED}
                valueStyle={{ color: '#ff4d4f', fontSize: 20 }}
                suffix="次"
              />
              <Statistic
                title="请假"
                value={statistics.LEAVE}
                valueStyle={{ color: '#1677ff', fontSize: 20 }}
                suffix="天"
              />
            </Space>
          </Col>
          <Col>
            <Space>
              <Button type="primary" icon={<CheckCircleOutlined />} onClick={() => handleClockIn(1)}>
                上班打卡
              </Button>
              <Button icon={<ClockCircleOutlined />} onClick={() => handleClockIn(2)}>
                下班打卡
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 考勤月历 */}
      <Card
        bordered={false}
        style={{ marginBottom: 16 }}
        title={
          <Space>
            <span>{currentMonth} 考勤日历</span>
          </Space>
        }
      >
        {calendarLoading ? (
          <Spin />
        ) : (
          <>
            {/* 星期头 */}
            <Row gutter={[4, 4]} style={{ marginBottom: 8 }}>
              {['一', '二', '三', '四', '五', '六', '日'].map((d) => (
                <Col span={3} key={d} style={{ textAlign: 'center' }}>
                  <Text strong type="secondary">
                    {d}
                  </Text>
                </Col>
              ))}
            </Row>
            {/* 日期网格 */}
            <Row gutter={[4, 4]}>
              {calendarDays.map((day) => {
                const dayNum = dayjs(day.date).date();
                const dayOfWeek = dayjs(day.date).day(); // 0=Sunday
                // 周日偏移处理：周日放最后
                const offset = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
                const isFirstRow = calendarDays.indexOf(day) < 7;

                return (
                  <Col span={3} key={day.date}>
                    <div
                      style={{
                        padding: '6px 4px',
                        borderRadius: 6,
                        textAlign: 'center',
                        backgroundColor: STATUS_BG_MAP[day.status] || '#fff',
                        border: '1px solid ' + (STATUS_COLOR_MAP[day.status] || '#f0f0f0'),
                        minHeight: 60,
                        cursor: 'pointer',
                      }}
                    >
                      <div style={{ fontWeight: 500, fontSize: 14 }}>{dayNum}</div>
                      <Tag
                        color={STATUS_COLOR_MAP[day.status] || 'default'}
                        style={{ fontSize: 11, padding: '0 4px', lineHeight: '18px' }}
                      >
                        {day.statusDesc}
                      </Tag>
                      {day.clockInTime && (
                        <div style={{ fontSize: 11, color: '#666' }}>↑{day.clockInTime}</div>
                      )}
                      {day.clockOutTime && (
                        <div style={{ fontSize: 11, color: '#666' }}>↓{day.clockOutTime}</div>
                      )}
                    </div>
                  </Col>
                );
              })}
            </Row>
          </>
        )}
      </Card>

      {/* 补卡记录 */}
      <Card
        bordered={false}
        title="补卡记录"
        extra={
          <Button type="primary" onClick={() => setMakeupModalOpen(true)}>
            申请补卡
          </Button>
        }
      >
        <Table
          dataSource={makeupRecords}
          columns={makeupColumns}
          rowKey="id"
          loading={makeupLoading}
          pagination={false}
          locale={{ emptyText: '暂无补卡记录' }}
        />
      </Card>

      {/* 补卡申请弹窗 */}
      <Modal
        title="申请补卡"
        open={makeupModalOpen}
        onOk={handleMakeupSubmit}
        onCancel={() => {
          setMakeupModalOpen(false);
          makeupForm.resetFields();
        }}
        destroyOnClose
      >
        <Form form={makeupForm} layout="vertical">
          <Form.Item
            name="correctionDate"
            label="补卡日期"
            rules={[{ required: true, message: '请选择补卡日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="correctionType"
            label="补卡类型"
            rules={[{ required: true, message: '请选择补卡类型' }]}
          >
            <Select>
              <Select.Option value="CLOCK_IN">上班卡</Select.Option>
              <Select.Option value="CLOCK_OUT">下班卡</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="correctionReason"
            label="补卡原因"
            rules={[{ required: true, message: '请输入补卡原因' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入补卡原因" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ProfileAttendancePage;

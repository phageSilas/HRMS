/**
 * 考勤统计栏 + 打卡按钮 + 申请请假
 */
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import { Button, Col, DatePicker, Row, Space, Statistic, Tooltip } from 'antd';
import dayjs from 'dayjs';
import React from 'react';

interface Props {
  currentMonth: string;
  onMonthChange: (month: string) => void;
  statistics: Record<string, number>;
  checkTodayStatus: { clockIn: boolean; clockOut: boolean; isHoliday: boolean };
  clockInDisabledReason: string;
  clockOutDisabledReason: string;
  onClockIn: (type: number) => void;
  onOpenLeaveModal: () => void;
}

const AttendanceStatsBar: React.FC<Props> = ({
  currentMonth,
  onMonthChange,
  statistics,
  checkTodayStatus,
  clockInDisabledReason,
  clockOutDisabledReason,
  onClockIn,
  onOpenLeaveModal,
}) => (
  <Row gutter={[16, 16]} align="middle">
    <Col>
      <DatePicker
        picker="month"
        value={dayjs(currentMonth, 'YYYY-MM')}
        onChange={(d) => d && onMonthChange(d.format('YYYY-MM'))}
        allowClear={false}
      />
    </Col>
    <Col flex="auto">
      <Space size="large" wrap>
        <Statistic
          title="出勤"
          value={statistics.NORMAL ?? 0}
          valueStyle={{ color: '#52c41a', fontSize: 20 }}
          suffix="天"
        />
        <Statistic
          title="迟到"
          value={statistics.LATE ?? 0}
          valueStyle={{ color: '#fa8c16', fontSize: 20 }}
          suffix="次"
        />
        <Statistic
          title="早退"
          value={statistics.EARLY_LEAVE ?? 0}
          valueStyle={{ color: '#faad14', fontSize: 20 }}
          suffix="次"
        />
        <Statistic
          title="缺卡"
          value={statistics.MISSED ?? 0}
          valueStyle={{ color: '#ff4d4f', fontSize: 20 }}
          suffix="次"
        />
        <Statistic
          title="请假"
          value={statistics.LEAVE ?? 0}
          valueStyle={{ color: '#1677ff', fontSize: 20 }}
          suffix="天"
        />
      </Space>
    </Col>
    <Col>
      <Space wrap>
        <Tooltip title={checkTodayStatus.clockIn ? '已完成上班打卡' : checkTodayStatus.isHoliday ? '今日为休息日' : '点击上班打卡'}>
          <span>
            <Button
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={() => onClockIn(1)}
              disabled={checkTodayStatus.clockIn || checkTodayStatus.isHoliday}
            >
              {checkTodayStatus.clockIn ? '已打卡' : '上班打卡'}
            </Button>
          </span>
        </Tooltip>
        <Tooltip title={clockOutDisabledReason || '点击下班打卡'}>
          <span>
            <Button
              icon={<ClockCircleOutlined />}
              onClick={() => onClockIn(2)}
              disabled={checkTodayStatus.clockOut || checkTodayStatus.isHoliday || !checkTodayStatus.clockIn}
            >
              {checkTodayStatus.clockOut ? '已打卡' : '下班打卡'}
            </Button>
          </span>
        </Tooltip>
        <Button type="default" icon={<PlusOutlined />} onClick={onOpenLeaveModal}>
          申请请假
        </Button>
      </Space>
    </Col>
  </Row>
);

export default AttendanceStatsBar;

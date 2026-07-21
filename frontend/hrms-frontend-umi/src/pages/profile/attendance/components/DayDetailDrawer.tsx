/**
 * 日期详情抽屉
 * 点击日历任意日期时，展示该日的详细考勤信息
 */
import { Button, Descriptions, Drawer, Space, Tag, Typography } from 'antd';
import dayjs from 'dayjs';
import React from 'react';
import { STATUS_COLOR_MAP } from '../constants';
import type { AttendanceDayVO } from '@/services/profile';

const { Text } = Typography;

interface Props {
  open: boolean;
  day: AttendanceDayVO | null;
  onClose: () => void;
  onOpenMakeup: (date: dayjs.Dayjs) => void;
}

const DayDetailDrawer: React.FC<Props> = ({ open, day, onClose, onOpenMakeup }) => {
  if (!day) return null;

  const dateObj = dayjs(day.date);
  const weekDay = dateObj.format('dddd');
  const weekDayMap: Record<string, string> = {
    Monday: '一', Tuesday: '二', Wednesday: '三', Thursday: '四',
    Friday: '五', Saturday: '六', Sunday: '日',
  };
  const isMissed = day.status === 'MISSED';

  const weekDayLabel = weekDayMap[weekDay] || weekDay;
  const dateLabel = `${dateObj.format('YYYY年M月D日')} 星期${weekDayLabel}`;

  return (
    <Drawer
      title={dateLabel}
      open={open}
      onClose={onClose}
      width={400}
      destroyOnClose
    >
      {/* 考勤状态 */}
      <div style={{ marginBottom: 24 }}>
        <Tag color={STATUS_COLOR_MAP[day.status] || 'default'} style={{ fontSize: 14, padding: '4px 12px' }}>
          {day.statusDesc}
        </Tag>
      </div>

      {/* 打卡时间 */}
      <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
        <Descriptions.Item label="上班打卡">
          {day.clockInTime ? (
            <Text style={{ color: '#52c41a' }}>{day.clockInTime}</Text>
          ) : (
            <Text type="secondary">未打卡</Text>
          )}
        </Descriptions.Item>
        <Descriptions.Item label="下班打卡">
          {day.clockOutTime ? (
            <Text style={{ color: '#1677ff' }}>{day.clockOutTime}</Text>
          ) : (
            <Text type="secondary">未打卡</Text>
          )}
        </Descriptions.Item>
        {day.leaveTypeDesc && (
          <Descriptions.Item label="请假类型">
            {day.leaveTypeDesc}
          </Descriptions.Item>
        )}
        {day.correctionStatus && day.correctionStatus !== 'NONE' && (
          <Descriptions.Item label="补卡状态">
            {day.correctionStatus === 'PENDING' ? (
              <Tag color="processing">审批中</Tag>
            ) : (
              <Tag color="success">已通过</Tag>
            )}
          </Descriptions.Item>
        )}
      </Descriptions>

      {/* 快捷操作 */}
      <Space direction="vertical" style={{ width: '100%' }}>
        {isMissed && (
          <Button
            type="primary"
            block
            onClick={() => {
              onOpenMakeup(dayjs(day.date));
              onClose();
            }}
          >
            申请补卡
          </Button>
        )}
      </Space>
    </Drawer>
  );
};

export default DayDetailDrawer;

/**
 * 日期详情抽屉
 * 点击日历任意日期时，展示该日的详细考勤信息
 */
import { Button, Descriptions, Drawer, Space, Tag, Typography } from 'antd';
import dayjs from 'dayjs';
import React from 'react';
import { STATUS_COLOR_MAP } from '../constants';
import type { AttendanceDayVO } from '@/services/profile';
import componentStyles from './style.less';

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
      <div className={componentStyles.drawerSection}>
        <Tag color={STATUS_COLOR_MAP[day.status] || 'default'} style={{ fontSize: 14, padding: '4px 12px' }}>
          {day.statusDesc}
        </Tag>
      </div>

      {/* 打卡时间 */}
      <Descriptions column={1} bordered size="small" className={componentStyles.drawerDescriptions}>
        <Descriptions.Item label="上班打卡">
          {day.clockInTime ? (
            <Text className={componentStyles.drawerClockIn}>{day.clockInTime}</Text>
          ) : (
            <Text type="secondary">未打卡</Text>
          )}
        </Descriptions.Item>
        <Descriptions.Item label="下班打卡">
          {day.clockOutTime ? (
            <Text className={componentStyles.drawerClockOut}>{day.clockOutTime}</Text>
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
      <Space direction="vertical" className={componentStyles.drawerActions}>
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

/**
 * 考勤日历网格
 * 始终渲染当前月份的完整日历，无数据时展示空白日期格
 */
import {
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { Button, Col, Empty, Row, Space, Spin, Tag, Tooltip, Typography } from 'antd';
import dayjs from 'dayjs';
import React, { useMemo } from 'react';
import { STATUS_BG_MAP, STATUS_COLOR_MAP, LEGEND_ITEMS, WEEKDAY_HEADERS } from '../constants';
import type { AttendanceDayVO } from '@/services/profile';
import componentStyles from './style.less';

const { Text } = Typography;

interface Props {
  days: AttendanceDayVO[];
  loading: boolean;
  error: string | null;
  currentMonth: string;
  firstDayOffset?: number; // 不再使用，由组件内部计算
  onRefresh: () => void;
  onDayClick: (day: AttendanceDayVO) => void;
  checkTodayStatus: { clockIn: boolean; clockOut: boolean; isHoliday: boolean };
  onClockIn: (type: number) => void;
}

/** 生成当月完整的日期列表（无考勤数据时的降级方案） */
function generateMonthDays(yearMonth: string): AttendanceDayVO[] {
  const ym = yearMonth.split('-');
  const year = parseInt(ym[0], 10);
  const month = parseInt(ym[1], 10);
  const daysInMonth = dayjs(`${year}-${month}-01`).daysInMonth();
  const result: AttendanceDayVO[] = [];

  for (let d = 1; d <= daysInMonth; d++) {
    const date = dayjs(`${year}-${month}-${String(d).padStart(2, '0')}`);
    const isWeekend = date.day() === 0 || date.day() === 6;
    result.push({
      date: date.format('YYYY-MM-DD'),
      status: isWeekend ? 'HOLIDAY' : 'NONE',
      statusDesc: isWeekend ? '休息日' : '',
    });
  }
  return result;
}

const AttendanceCalendar: React.FC<Props> = ({
  days,
  loading,
  error,
  currentMonth,
  onRefresh,
  onDayClick,
  checkTodayStatus,
  onClockIn,
}) => {
  // 始终根据当前月份的第一天计算星期偏移（周一=0, 周日=6）
  const monthFirstDay = dayjs(currentMonth + '-01');
  const firstDayOffset = monthFirstDay.day() === 0 ? 6 : monthFirstDay.day() - 1;

  // 无数据时生成虚拟日期（确保日历始终显示完整的月视图）
  const displayDays = useMemo<AttendanceDayVO[]>(() => {
    if (days.length > 0) {
      return days;
    }
    return generateMonthDays(currentMonth);
  }, [days, currentMonth]);

  // ============ Loading ============
  if (loading) {
    return (
      <div className={componentStyles.calendarLoading}>
        <Spin />
        <div className={componentStyles.calendarLoadingText}>加载考勤日历数据...</div>
      </div>
    );
  }

  // ============ Error ============
  if (error) {
    return (
      <div className={componentStyles.calendarError}>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <Space direction="vertical" size={8}>
              <Text type="warning">
                <ExclamationCircleOutlined className={componentStyles.calendarErrorIcon} />
                获取考勤日历数据失败
              </Text>
              <Text type="secondary" className={componentStyles.calendarErrorText}>{error}</Text>
              <Button size="small" onClick={onRefresh}>重新加载</Button>
            </Space>
          }
        />
      </div>
    );
  }

  // ============ Calendar Grid（始终渲染） ============
  return (
    <>
      {/* 星期头 */}
      <Row gutter={[4, 4]} className={componentStyles.calendarGrid}>
        {WEEKDAY_HEADERS.map((d, idx) => (
          <Col span={3} key={d} className={componentStyles.weekdayCell}>
            <Text strong type="secondary" className={idx >= 5 ? componentStyles.weekdayWeekend : componentStyles.weekdayLabel}>
              {d}
            </Text>
          </Col>
        ))}
      </Row>

      {/* 日期网格 */}
      <Row gutter={[4, 4]}>
        {firstDayOffset > 0 &&
          Array.from({ length: firstDayOffset }).map((_, i) => (
            <Col span={3} key={`offset-${i}`} />
          ))}
        {displayDays.map((day) => {
          const dayNum = dayjs(day.date).date();
          const isToday = day.date === dayjs().format('YYYY-MM-DD');
          const isWeekend = day.status === 'HOLIDAY';
          const hasData = day.status !== 'NONE' && day.status !== 'HOLIDAY' && day.status !== '';

          // Tooltip 内容
          const tooltipContent = [
            `${day.date}${day.statusDesc ? ' ' + day.statusDesc : ''}`,
            day.clockInTime ? `上班：${day.clockInTime}` : '',
            day.clockOutTime ? `下班：${day.clockOutTime}` : '',
            day.leaveTypeDesc ? `请假：${day.leaveTypeDesc}` : '',
            day.correctionStatus && day.correctionStatus !== 'NONE'
              ? `补卡：${day.correctionStatus === 'PENDING' ? '审批中' : '已通过'}`
              : '',
          ].filter(Boolean).join('\n') || `${day.date}`;

          return (
            <Col span={3} key={day.date}>
              <Tooltip title={tooltipContent}>
                <div
                  style={{
                    backgroundColor: isToday
                      ? '#e6f4ff'
                      : hasData
                        ? STATUS_BG_MAP[day.status] || '#fff'
                        : '#fff',
                    border: isToday
                      ? '2px solid #1677ff'
                      : hasData
                        ? `1px solid ${STATUS_COLOR_MAP[day.status] || '#f0f0f0'}`
                        : '1px solid #f0f0f0',
                    opacity: isWeekend ? 0.6 : 1,
                  }}
                  className={
                    isWeekend
                      ? componentStyles.dayCell + ' ' + componentStyles.dayCellEmpty
                      : componentStyles.dayCell + (isToday ? ' ' + componentStyles.dayCellToday : '')
                  }
                  onClick={() => onDayClick(day)}
                  onMouseEnter={(e) => {
                    if (!isWeekend) {
                      (e.currentTarget as HTMLElement).style.transform = 'scale(1.05)';
                      (e.currentTarget as HTMLElement).style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                    }
                  }}
                  onMouseLeave={(e) => {
                    (e.currentTarget as HTMLElement).style.transform = 'scale(1)';
                    (e.currentTarget as HTMLElement).style.boxShadow = 'none';
                  }}
                >
                  <div
                    style={{
                      fontWeight: isToday ? 700 : 500,
                      fontSize: 14,
                      color: isToday ? '#1677ff' : undefined,
                    }}
                  >
                    {dayNum}
                  </div>
                  {hasData ? (
                    <Tag
                      color={STATUS_COLOR_MAP[day.status] || 'default'}
                      className={componentStyles.dayStatusTag}
                    >
                      {day.statusDesc}
                    </Tag>
                  ) : !isWeekend ? (
                    <div className={componentStyles.dayStatusText}>--</div>
                  ) : null}
                  {day.clockInTime && (
                    <div className={componentStyles.dayClockIn}>↑{day.clockInTime}</div>
                  )}
                  {day.clockOutTime && (
                    <div className={componentStyles.dayClockOut}>↓{day.clockOutTime}</div>
                  )}
                </div>
              </Tooltip>
            </Col>
          );
        })}
      </Row>

      {/* 图例 */}
      <div className={componentStyles.legendBar}>
        {LEGEND_ITEMS.map((item) => (
          <div key={item.label} className={componentStyles.legendItem}>
            <div
              className={componentStyles.legendDot}
              style={{
                backgroundColor: item.isBorder ? 'transparent' : item.color,
                border: item.isBorder ? `2px solid ${item.color}` : 'none',
              }}
            />
            <Text type="secondary" className={componentStyles.legendLabel}>{item.label}</Text>
          </div>
        ))}
      </div>
    </>
  );
};

export default AttendanceCalendar;

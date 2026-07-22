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
      console.log('[DEBUG Calendar] displayDays(API):', days.length, '条, 首条状态:', days[0]?.status, '首条日期:', days[0]?.date);
      return days;
    }
    console.log('[DEBUG Calendar] displayDays(虚拟): 使用generateMonthDays');
    return generateMonthDays(currentMonth);
  }, [days, currentMonth]);

  // ============ Loading ============
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '40px 0' }}>
        <Spin />
        <div style={{ marginTop: 12, color: '#999' }}>加载考勤日历数据...</div>
      </div>
    );
  }

  // ============ Error ============
  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '40px 0' }}>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <Space direction="vertical" size={8}>
              <Text type="warning">
                <ExclamationCircleOutlined style={{ marginRight: 6 }} />
                获取考勤日历数据失败
              </Text>
              <Text type="secondary" style={{ fontSize: 13 }}>{error}</Text>
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
      <Row gutter={[4, 4]} style={{ marginBottom: 8 }}>
        {WEEKDAY_HEADERS.map((d, idx) => (
          <Col key={d} style={{ flex: '0 0 calc(100% / 7)', maxWidth: 'calc(100% / 7)', textAlign: 'center' }}>
            <Text strong type="secondary" style={{ color: idx >= 5 ? '#ff4d4f' : undefined }}>
              {d}
            </Text>
          </Col>
        ))}
      </Row>

      {/* 日期网格 */}
      <Row gutter={[4, 4]}>
        {firstDayOffset > 0 &&
          Array.from({ length: firstDayOffset }).map((_, i) => (
            <Col key={`offset-${i}`} style={{ flex: '0 0 calc(100% / 7)', maxWidth: 'calc(100% / 7)' }} />
          ))}
        {displayDays.map((day) => {
          const dayNum = dayjs(day.date).date();
          const isToday = day.date === dayjs().format('YYYY-MM-DD');
          const isWeekend = day.status === 'HOLIDAY';
          const hasData = day.status !== 'NONE' && day.status !== 'HOLIDAY' && day.status !== '';

          // Tooltip 内容
          let tooltipLines: string[];
          if (day.status === 'NORMAL' || day.status === 'LATE' || day.status === 'EARLY_LEAVE') {
            // 正常/迟到/早退：第一行日期，第二行上下班时间
            const timeParts = [
              day.clockInTime ? `上班：${day.clockInTime}` : '',
              day.clockOutTime ? `下班：${day.clockOutTime}` : '',
            ].filter(Boolean);
            tooltipLines = [day.date];
            if (timeParts.length) {
              tooltipLines.push(timeParts.join('  '));
            }
          } else {
            // 请假、无完整打卡记录等：第一行日期，第二行状态
            tooltipLines = [day.date, day.statusDesc || ''];
            if (day.leaveTypeDesc) {
              tooltipLines.push(day.leaveTypeDesc);
            }
          }
          if (day.correctionStatus && day.correctionStatus !== 'NONE') {
            tooltipLines.push(`补卡：${day.correctionStatus === 'PENDING' ? '审批中' : '已通过'}`);
          }
          const tooltipContent = tooltipLines.filter(Boolean).join('\n');

          return (
            <Col key={day.date} style={{ flex: '0 0 calc(100% / 7)', maxWidth: 'calc(100% / 7)' }}>
              <Tooltip title={tooltipContent}>
                <div
                  style={{
                    padding: '6px 4px',
                    borderRadius: 6,
                    textAlign: 'center',
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
                    minHeight: 60,
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    position: 'relative',
                    opacity: isWeekend ? 0.6 : 1,
                  }}
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
                      style={{ fontSize: 11, padding: '0 4px', lineHeight: '18px' }}
                    >
                      {day.statusDesc}
                    </Tag>
                  ) : !isWeekend ? (
                    <div style={{ fontSize: 11, color: '#ccc', marginTop: 2 }}>--</div>
                  ) : null}
                  {day.clockInTime && (
                    <div style={{ fontSize: 11, color: '#52c41a' }}>↑{day.clockInTime}</div>
                  )}
                  {day.clockOutTime && (
                    <div style={{ fontSize: 11, color: '#1677ff' }}>↓{day.clockOutTime}</div>
                  )}
                </div>
              </Tooltip>
            </Col>
          );
        })}
      </Row>

      {/* 图例 */}
      <div style={{ marginTop: 16, display: 'flex', gap: 16, justifyContent: 'center', flexWrap: 'wrap' }}>
        {LEGEND_ITEMS.map((item) => (
          <div key={item.label} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <div
              style={{
                width: 14,
                height: 14,
                borderRadius: 3,
                backgroundColor: item.isBorder ? 'transparent' : item.color,
                border: item.isBorder ? `2px solid ${item.color}` : 'none',
              }}
            />
            <Text type="secondary" style={{ fontSize: 12 }}>{item.label}</Text>
          </div>
        ))}
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <Text style={{ fontSize: 12, color: '#52c41a', fontWeight: 500 }}>↑</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>上班</Text>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <Text style={{ fontSize: 12, color: '#1677ff', fontWeight: 500 }}>↓</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>下班</Text>
        </div>
      </div>
    </>
  );
};

export default AttendanceCalendar;

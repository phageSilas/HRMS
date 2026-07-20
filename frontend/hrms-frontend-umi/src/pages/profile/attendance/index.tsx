/**
 * 我的考勤页面
 * 考勤日历月视图 + 打卡 + 请假申请 + 补卡申请 + 加班申请
 */
import { PageContainer } from '@ant-design/pro-components';
import { Button, Card, Space, message } from 'antd';
import dayjs from 'dayjs';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  clockIn,
  createLeave,
  createMakeup,
  createOvertime,
  getAttendanceCalendar,
  getMakeupRecords,
  getOvertimeRecords,
} from '@/services/profile';
import type {
  AttendanceCalendarVO,
  AttendanceDayVO,
  MakeupRecordVO,
  OvertimeRecordVO,
} from '@/services/profile';
import AttendanceStatsBar from './components/AttendanceStatsBar';
import AttendanceCalendar from './components/AttendanceCalendar';
import DayDetailDrawer from './components/DayDetailDrawer';
import MakeupModal from './components/MakeupModal';
import MakeupRecordsTable from './components/MakeupRecordsTable';
import OvertimeModal from './components/OvertimeModal';
import OvertimeRecordsTable from './components/OvertimeRecordsTable';
import LeaveModal from './components/LeaveModal';
import styles from './style.less';

const ProfileAttendancePage: React.FC = () => {
  const [currentMonth, setCurrentMonth] = useState(dayjs().format('YYYY-MM'));
  const [calendarError, setCalendarError] = useState<string | null>(null);
  const [clockInLoading, setClockInLoading] = useState(false);

  // —— 弹窗状态 ——
  const [detailDay, setDetailDay] = useState<AttendanceDayVO | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [makeupOpen, setMakeupOpen] = useState(false);
  const [makeupInitialDate, setMakeupInitialDate] = useState<dayjs.Dayjs | undefined>();
  const [overtimeOpen, setOvertimeOpen] = useState(false);
  const [leaveOpen, setLeaveOpen] = useState(false);

  // —— 强制刷新回调 ——
  // ==================== 数据加载（改用 useState+useEffect 替代 useRequest） ====================

  const [calendarData, setCalendarData] = useState<AttendanceCalendarVO | null>(null);
  const [calendarLoading, setCalendarLoading] = useState(false);
  const [makeupRecords, setMakeupRecords] = useState<MakeupRecordVO[]>([]);
  const [makeupLoading, setMakeupLoading] = useState(false);
  const [overtimeRecords, setOvertimeRecords] = useState<OvertimeRecordVO[]>([]);
  const [overtimeLoading, setOvertimeLoading] = useState(false);

  // 加载触发
  const [loadKey, setLoadKey] = useState(0);

  const fetchAll = useCallback(async () => {
    // 日历
    try {
      setCalendarLoading(true);
      const cal = await getAttendanceCalendar(currentMonth);
      setCalendarData(cal);
      setCalendarError(null);
    } catch (err: any) {
      setCalendarError(err?.message || '获取考勤日历失败');
    } finally {
      setCalendarLoading(false);
    }

    // 补卡记录
    try {
      setMakeupLoading(true);
      const mk = await getMakeupRecords();
      setMakeupRecords(mk || []);
    } catch {
      setMakeupRecords([]);
    } finally {
      setMakeupLoading(false);
    }

    // 加班记录
    try {
      setOvertimeLoading(true);
      const ot = await getOvertimeRecords();
      setOvertimeRecords(ot || []);
    } catch {
      setOvertimeRecords([]);
    } finally {
      setOvertimeLoading(false);
    }
  }, [currentMonth]);

  // 首次加载 + currentMonth/loadKey 变化时重新加载
  useEffect(() => { fetchAll(); }, [fetchAll, loadKey]);

  // 自动刷新（每分钟）
  useEffect(() => {
    const timer = setInterval(fetchAll, 60000);
    return () => clearInterval(timer);
  }, [fetchAll]);

  // ============ 日历天数 & 统计 ============

  const calendarDays = useMemo<AttendanceDayVO[]>(() => {
    const days = calendarData?.days || [];
    return days;
  }, [calendarData]);

  const statistics = useMemo(() => {
    const stats: Record<string, number> = {
      NORMAL: 0, LATE: 0, EARLY_LEAVE: 0, MISSED: 0, LEAVE: 0, HOLIDAY: 0, ABSENT: 0,
    };
    calendarDays.forEach((day) => {
      if (stats[day.status] !== undefined) stats[day.status]++;
    });
    return stats;
  }, [calendarDays]);

  // ============ 打卡 ============

  const checkTodayStatus = useMemo(() => {
    const todayStr = dayjs().format('YYYY-MM-DD');
    const today = calendarDays.find((d) => d.date === todayStr);
    if (!today) return { clockIn: false, clockOut: false, isHoliday: false };
    return {
      clockIn: !!today.clockInTime,
      clockOut: !!today.clockOutTime,
      isHoliday: today.status === 'HOLIDAY',
    };
  }, [calendarDays]);

  const clockOutDisabledReason = useMemo(() => {
    if (checkTodayStatus.clockOut) return '已完成下班打卡';
    if (checkTodayStatus.isHoliday) return '今日为休息日';
    if (!checkTodayStatus.clockIn) return '请先进行上班打卡';
    return '';
  }, [checkTodayStatus]);

  const clockInDisabledReason = useMemo(() => {
    if (checkTodayStatus.clockIn) return '已完成上班打卡';
    if (checkTodayStatus.isHoliday) return '今日为休息日';
    return '';
  }, [checkTodayStatus]);

  const handleClockIn = async (type: number) => {
    if (clockInLoading) return;
    setClockInLoading(true);
    try {
      if (type === 1 && checkTodayStatus.clockIn) {
        message.info('今天已完成上班打卡');
        return;
      }
      if (type === 2 && checkTodayStatus.clockOut) {
        message.info('今天已完成下班打卡');
        return;
      }
      await clockIn({ type });
      message.success(type === 1 ? '上班打卡成功' : '下班打卡成功');
      setLoadKey((k) => k + 1);
    } catch {
      // 错误由 request 拦截器统一处理
    } finally {
      setClockInLoading(false);
    }
  };

  // ============ 提交回调 ============

  const handleMakeupSubmit = async (values: any) => {
    const payload = {
      correctionDate: values.correctionDate.format('YYYY-MM-DD'),
      correctionType: values.correctionType,
      correctionReason: values.correctionReason,
    };
    await createMakeup(payload);
    message.success('补卡申请已提交');
    setMakeupOpen(false);
    setLoadKey((k) => k + 1);
  };

  const handleOvertimeSubmit = async (values: any) => {
    const payload = {
      overtimeDate: values.overtimeDate.format('YYYY-MM-DDTHH:mm:ss'),
      duration: values.duration,
      reason: values.reason,
    };
    await createOvertime(payload);
    message.success('加班申请已提交');
    setOvertimeOpen(false);
    setLoadKey((k) => k + 1);
  };

  const handleLeaveSubmit = async (values: any) => {
    const startTime = values.dateRange[0];
    const endTime = values.dateRange[1];
    const totalDays = endTime.diff(startTime, 'day') + 1;
    const payload = {
      leaveType: values.leaveType,
      startTime: startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: endTime.format('YYYY-MM-DD HH:mm:ss'),
      totalDays,
      leaveReason: values.leaveReason,
    };
    await createLeave(payload);
    message.success('请假申请已提交');
    setLeaveOpen(false);
    setLoadKey((k) => k + 1);
  };

  // ============ 日历交互 ============

  const handleDayClick = (day: AttendanceDayVO) => {
    setDetailDay(day);
    setDetailOpen(true);
  };

  const handleOpenMakeupFromCalendar = (date: dayjs.Dayjs) => {
    setMakeupInitialDate(date);
    setMakeupOpen(true);
  };

  // ============ 渲染 ============

  return (
    <PageContainer>
      <Card bordered={false} className={styles.cardMargin}>
        <AttendanceStatsBar
          currentMonth={currentMonth}
          onMonthChange={setCurrentMonth}
          statistics={statistics}
          checkTodayStatus={checkTodayStatus}
          clockInDisabledReason={clockInDisabledReason}
          clockOutDisabledReason={clockOutDisabledReason}
          onClockIn={handleClockIn}
          onOpenLeaveModal={() => setLeaveOpen(true)}
        />
      </Card>

      <Card
        bordered={false}
        className={styles.cardMargin}
        title={<Space><span>{currentMonth} 考勤日历</span></Space>}
      >
        <AttendanceCalendar
          days={calendarDays}
          loading={calendarLoading}
          error={calendarError}
          currentMonth={currentMonth}
          onRefresh={() => { setCalendarError(null); setLoadKey((k) => k + 1); }}
          onDayClick={handleDayClick}
          checkTodayStatus={checkTodayStatus}
          onClockIn={handleClockIn}
        />
      </Card>

      <Card
        bordered={false}
        title="补卡记录"
        extra={<Button type="primary" onClick={() => setMakeupOpen(true)}>申请补卡</Button>}
      >
        <MakeupRecordsTable records={makeupRecords} loading={makeupLoading} />
      </Card>

      <Card
        bordered={false}
        title="加班记录"
        className={styles.cardMarginTop}
        extra={<Button type="primary" onClick={() => setOvertimeOpen(true)}>申请加班</Button>}
      >
        <OvertimeRecordsTable records={overtimeRecords} loading={overtimeLoading} />
      </Card>

      {/* ==================== 弹窗/抽屉 ==================== */}
      <DayDetailDrawer
        open={detailOpen}
        day={detailDay}
        onClose={() => setDetailOpen(false)}
        onOpenMakeup={handleOpenMakeupFromCalendar}
      />
      <MakeupModal
        open={makeupOpen}
        onClose={() => setMakeupOpen(false)}
        onSubmit={handleMakeupSubmit}
        initialDate={makeupInitialDate}
      />
      <OvertimeModal
        open={overtimeOpen}
        onClose={() => setOvertimeOpen(false)}
        onSubmit={handleOvertimeSubmit}
      />
      <LeaveModal
        open={leaveOpen}
        onClose={() => setLeaveOpen(false)}
        onSubmit={handleLeaveSubmit}
      />
    </PageContainer>
  );
};

export default ProfileAttendancePage;

import {
  CheckCircleFilled,
  ClockCircleOutlined,
  EnvironmentOutlined,
  InfoCircleFilled,
  RightOutlined,
  SafetyCertificateFilled,
} from '@ant-design/icons';
import { history, useRequest } from '@umijs/max';
import { Button, Card, Col, Row, Space, Tag, Typography, message } from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import {
  clockAttendance,
  getMyAttendanceCalendar,
} from '@/services/attendance';
import type { AttendanceCalendarDayVO } from '@/services/attendance';
import styles from './index.less';

const { Text, Title } = Typography;

const STATUS_LABEL_MAP: Record<string, string> = {
  NORMAL: '正常',
  LATE: '迟到',
  EARLY_LEAVE: '早退',
  MISSED: '缺卡',
  ABSENT: '旷工',
  LEAVE: '请假',
  HOLIDAY: '休息',
};

const STATUS_COLOR_MAP: Record<string, string> = {
  NORMAL: 'success',
  LATE: 'warning',
  EARLY_LEAVE: 'warning',
  MISSED: 'error',
  ABSENT: 'error',
  LEAVE: 'processing',
  HOLIDAY: 'default',
};

function formatTime(value?: string) {
  if (!value) return '--:--';
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('HH:mm') : value.slice(11, 16) || value;
}

function getStatusLabel(status?: string) {
  if (!status) return '未打卡';
  return STATUS_LABEL_MAP[status] || status;
}

function getDeviceInfo() {
  const platform = navigator.platform || 'unknown';
  const language = navigator.language || 'unknown';
  return `web:${platform};lang:${language}`;
}

async function resolveLocation() {
  if (!navigator.geolocation) {
    return {};
  }

  return new Promise<{ latitude?: number; longitude?: number }>((resolve) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: Number(position.coords.latitude.toFixed(6)),
          longitude: Number(position.coords.longitude.toFixed(6)),
        });
      },
      () => resolve({}),
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 60000,
      },
    );
  });
}

const AttendancePunchPage: React.FC = () => {
  const [now, setNow] = useState(dayjs());
  const currentMonth = now.format('YYYY-MM');

  const {
    data: calendar,
    loading,
    refresh,
  } = useRequest(() => getMyAttendanceCalendar(currentMonth), {
    refreshDeps: [currentMonth],
  });

  const { run: submitClock, loading: clocking } = useRequest(
    async (type: 'CLOCK_IN' | 'CLOCK_OUT') => {
      const location = await resolveLocation();
      return clockAttendance({
        type,
        ...location,
        deviceInfo: getDeviceInfo(),
      });
    },
    {
      manual: true,
      onSuccess: (result) => {
        const label = result.period === 'CLOCK_OUT' ? '下班打卡' : '上班打卡';
        message.success(`${label}成功`);
        refresh();
      },
    },
  );

  useEffect(() => {
    const timer = window.setInterval(() => setNow(dayjs()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  const todayRecord = useMemo<AttendanceCalendarDayVO | undefined>(() => {
    const today = now.format('YYYY-MM-DD');
    return calendar?.days?.find((item) => dayjs(item.date).format('YYYY-MM-DD') === today);
  }, [calendar?.days, now]);

  const clockInDone = Boolean(todayRecord?.clockInTime);
  const clockOutDone = Boolean(todayRecord?.clockOutTime);
  const nextClockType: 'CLOCK_IN' | 'CLOCK_OUT' = clockInDone ? 'CLOCK_OUT' : 'CLOCK_IN';
  const nextClockText = clockInDone ? '下班打卡' : '上班打卡';
  const dayStatus = todayRecord?.dayStatus || (clockInDone || clockOutDone ? 'NORMAL' : undefined);

  return (
    <div className={styles.punchPage}>
      <div className={styles.pageTitle}>
        <div className={styles.breadcrumbText}>首页 / 考勤管理 / 员工打卡</div>
        <Title level={2} style={{ margin: 0 }}>
          员工打卡
        </Title>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={15}>
          <Card className={styles.clockCard} bordered={false} loading={loading}>
            <div className={styles.clockHero}>
              <div className={styles.dateLine}>
                {now.format('YYYY-MM-DD')}　{now.format('dddd')}
              </div>
              <div className={styles.timeText}>{now.format('HH:mm')}</div>
              <div className={styles.groupLine}>
                所属考勤组：<span className={styles.groupName}>标准工时组</span>
              </div>

              <Row gutter={[24, 24]} className={styles.shiftCards}>
                <Col xs={24} md={12}>
                  <div className={`${styles.shiftCard} ${clockInDone ? styles.shiftDone : ''}`}>
                    <span className={`${styles.shiftIcon} ${clockInDone ? styles.shiftIconDone : ''}`}>
                      <ClockCircleOutlined />
                    </span>
                    <div>
                      <div className={styles.shiftLabel}>上班打卡</div>
                      <div className={clockInDone ? styles.shiftTime : styles.shiftPending}>
                        {formatTime(todayRecord?.clockInTime)}
                      </div>
                      <Tag color={clockInDone ? STATUS_COLOR_MAP[todayRecord?.clockInStatus || 'NORMAL'] : 'default'}>
                        {clockInDone ? getStatusLabel(todayRecord?.clockInStatus || 'NORMAL') : '待打卡'}
                      </Tag>
                    </div>
                  </div>
                </Col>
                <Col xs={24} md={12}>
                  <div className={`${styles.shiftCard} ${clockOutDone ? styles.shiftDone : ''}`}>
                    <span className={`${styles.shiftIcon} ${clockOutDone ? styles.shiftIconDone : ''}`}>
                      <ClockCircleOutlined />
                    </span>
                    <div>
                      <div className={styles.shiftLabel}>下班打卡</div>
                      <div className={clockOutDone ? styles.shiftTime : styles.shiftPending}>
                        {formatTime(todayRecord?.clockOutTime)}
                      </div>
                      <Tag color={clockOutDone ? STATUS_COLOR_MAP[todayRecord?.clockOutStatus || 'NORMAL'] : 'default'}>
                        {clockOutDone ? getStatusLabel(todayRecord?.clockOutStatus || 'NORMAL') : '待打卡'}
                      </Tag>
                    </div>
                  </div>
                </Col>
              </Row>

              <Button
                type="primary"
                size="large"
                className={styles.punchButton}
                icon={<EnvironmentOutlined />}
                loading={clocking}
                disabled={clockInDone && clockOutDone}
                onClick={() => submitClock(nextClockType)}
              >
                {clockInDone && clockOutDone ? '今日已完成打卡' : nextClockText}
              </Button>

              <div className={styles.ruleLine}>
                今日上班时间：09:00　|　规定上班时间：09:00
              </div>

              <div className={styles.footerStatus}>
                <Space>
                  <EnvironmentOutlined style={{ color: '#1fbf63', fontSize: 22 }} />
                  定位状态：点击打卡时获取
                </Space>
                <Space>
                  <SafetyCertificateFilled style={{ color: '#1fbf63', fontSize: 22 }} />
                  IP 校验：由后端自动校验
                </Space>
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={9}>
          <Card className={styles.recordCard} bordered={false}>
            <div className={styles.recordHeader}>
              <Title level={4} style={{ margin: 0 }}>
                今日考勤记录
              </Title>
              <Button type="link" onClick={() => history.push('/profile/attendance')}>
                查看月度记录 <RightOutlined />
              </Button>
            </div>

            <div className={styles.timeline}>
              <div className={styles.timelineItem}>
                <span className={`${styles.timelineDot} ${clockInDone ? styles.timelineDotDone : ''}`}>
                  {clockInDone && <CheckCircleFilled />}
                </span>
                <div>
                  <div className={styles.recordTime}>{formatTime(todayRecord?.clockInTime)}</div>
                  <div className={styles.recordMeta}>
                    上班打卡　
                    <Tag color={clockInDone ? STATUS_COLOR_MAP[todayRecord?.clockInStatus || 'NORMAL'] : 'default'}>
                      {clockInDone ? getStatusLabel(todayRecord?.clockInStatus || 'NORMAL') : '未打卡'}
                    </Tag>
                  </div>
                </div>
                <Text type="secondary">公司网络</Text>
              </div>

              <div className={styles.timelineItem}>
                <span className={`${styles.timelineDot} ${clockOutDone ? styles.timelineDotDone : ''}`}>
                  {clockOutDone && <CheckCircleFilled />}
                </span>
                <div>
                  <div className={styles.recordTime}>{formatTime(todayRecord?.clockOutTime)}</div>
                  <div className={styles.recordMeta}>
                    下班打卡　
                    <Tag color={clockOutDone ? STATUS_COLOR_MAP[todayRecord?.clockOutStatus || 'NORMAL'] : 'default'}>
                      {clockOutDone ? getStatusLabel(todayRecord?.clockOutStatus || 'NORMAL') : '未打卡'}
                    </Tag>
                  </div>
                </div>
                <Text type="secondary">--</Text>
              </div>
            </div>

            <div className={styles.tipsBox}>
              <Space align="start">
                <InfoCircleFilled style={{ color: '#0b63f6', marginTop: 3 }} />
                <div>
                  <Text strong>温馨提示</Text>
                  <div style={{ marginTop: 8, color: '#66758c' }}>
                    请在规定时间内完成打卡，如遇问题请及时联系考勤管理员。
                    {dayStatus && (
                      <>
                        <br />
                        今日综合状态：{getStatusLabel(dayStatus)}
                      </>
                    )}
                  </div>
                </div>
              </Space>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AttendancePunchPage;

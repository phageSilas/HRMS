import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type {
  AttendanceCalendarDayVO,
  AttendanceClockVO,
} from '@/services/attendance';
import {
  clockAttendance,
  getMyAttendanceCalendar,
} from '@/services/attendance';
import type {
  AmapLocationResult,
  AmapLocationStatus,
} from '@/utils/amapLocation';
import { resolveAmapLocation } from '@/utils/amapLocation';
import {
  CheckCircleFilled,
  ClockCircleOutlined,
  EnvironmentOutlined,
  InfoCircleFilled,
  RightOutlined,
  SafetyCertificateFilled,
} from '@ant-design/icons';
import { history, useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  Modal,
  Row,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
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
  NONE: '未打卡',
};

const STATUS_COLOR_MAP: Record<string, string> = {
  NORMAL: 'success',
  LATE: 'warning',
  EARLY_LEAVE: 'warning',
  MISSED: 'error',
  ABSENT: 'error',
  LEAVE: 'processing',
  HOLIDAY: 'default',
  NONE: 'default',
};

interface LocationState {
  status: AmapLocationStatus;
  text: string;
  detail?: string;
  location?: AmapLocationResult;
}

type ClockPeriod = 'CLOCK_IN' | 'CLOCK_OUT';
type BackendDateValue = string | number[] | Date | undefined | null;
type ClockResponsePayload =
  | AttendanceClockVO
  | {
      code?: number;
      data?: AttendanceClockVO;
      message?: string;
    }
  | undefined
  | null;

const LOCATION_STATUS_COLOR: Record<AmapLocationStatus, string> = {
  idle: 'default',
  locating: 'processing',
  success: 'success',
  failed: 'warning',
};

function parseBackendDate(value?: BackendDateValue) {
  if (!value) return undefined;

  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] =
      value;
    if (!year || !month || !day) return undefined;
    return dayjs(
      new Date(
        year,
        month - 1,
        day,
        hour,
        minute,
        second,
        Math.floor(nano / 1_000_000),
      ),
    );
  }

  const parsed = dayjs(value);
  return parsed.isValid() ? parsed : undefined;
}

function normalizeDate(value?: BackendDateValue) {
  return parseBackendDate(value)?.format('YYYY-MM-DD');
}

function normalizeDateTime(value?: BackendDateValue) {
  return parseBackendDate(value)?.format('YYYY-MM-DDTHH:mm:ss');
}

function formatTime(value?: BackendDateValue) {
  return parseBackendDate(value)?.format('HH:mm') || '--:--';
}

function getStatusLabel(status?: string) {
  if (!status) return '未打卡';
  return STATUS_LABEL_MAP[status] || status;
}

function getClockLabel(period?: string) {
  return period === 'CLOCK_OUT' ? '下班打卡' : '上班打卡';
}

function getClockIp(result?: AttendanceClockVO) {
  return result?.networkIp || result?.clientIp;
}

function unwrapClockResponse(payload: ClockResponsePayload) {
  if (!payload) return undefined;
  if ('data' in payload && payload.data) {
    return payload.data;
  }
  return payload as AttendanceClockVO;
}

function normalizeClockResult(
  payload: ClockResponsePayload,
  fallbackPeriod: ClockPeriod,
) {
  const data = unwrapClockResponse(payload);
  const fallbackNow = dayjs();

  return {
    recordId: data?.recordId || 0,
    employeeId: data?.employeeId || 0,
    groupId: data?.groupId,
    recordDate:
      normalizeDate(data?.recordDate as BackendDateValue) ||
      fallbackNow.format('YYYY-MM-DD'),
    period: data?.period || fallbackPeriod,
    status: data?.status || 'NORMAL',
    clockTime:
      normalizeDateTime(data?.clockTime as BackendDateValue) ||
      fallbackNow.format('YYYY-MM-DDTHH:mm:ss'),
    networkIp: data?.networkIp,
    clientIp: data?.clientIp,
  } satisfies AttendanceClockVO;
}

function formatLocationText(location?: AmapLocationResult) {
  if (!location) return '未获取定位，以后端校验结果为准';
  return `经度 ${location.longitude.toFixed(6)}，纬度 ${location.latitude.toFixed(6)}`;
}

function formatLocationDetail(location?: AmapLocationResult) {
  if (!location) return undefined;
  const accuracy =
    location.accuracy == null ? '未知' : `${location.accuracy} 米`;
  return `精度 ${accuracy}，类型 ${location.locationType || '未知'}`;
}

function getDeviceInfo(location?: AmapLocationResult) {
  const platform = navigator.platform || 'unknown';
  const language = navigator.language || 'unknown';
  const browser = navigator.userAgent.includes('Edg')
    ? 'Edge'
    : navigator.userAgent.includes('Chrome')
    ? 'Chrome'
    : 'Browser';
  const locationInfo = location
    ? `;amap=${location.locationType || 'unknown'};accuracy=${
        location.accuracy ?? 'unknown'
      }m`
    : ';amap=unavailable';
  return `web:${browser}-${platform};lang:${language}${locationInfo}`;
}

const AttendancePunchPage: React.FC = () => {
  const [now, setNow] = useState(dayjs());
  const currentDate = now.format('YYYY-MM-DD');
  const currentMonth = now.format('YYYY-MM');
  const [clocking, setClocking] = useState(false);
  const [locationState, setLocationState] = useState<LocationState>({
    status: 'idle',
    text: '点击打卡时获取高德定位',
  });

  const {
    data: calendar,
    loading,
    refresh,
  } = useRequest(() => getMyAttendanceCalendar(currentMonth), {
    refreshDeps: [currentMonth],
  });

  const calendarData = calendar as
    | {
        days?: AttendanceCalendarDayVO[];
      }
    | undefined;

  usePageAutoRefresh(() => {
    refresh();
  });

  const todayRecord = useMemo<AttendanceCalendarDayVO | undefined>(() => {
    return calendarData?.days?.find(
      (item) => normalizeDate(item.date as BackendDateValue) === currentDate,
    );
  }, [calendarData?.days, currentDate]);

  const handleSubmitClock = async (type: ClockPeriod) => {
    if (clocking) return;

    setClocking(true);
    let location: AmapLocationResult | undefined;

    try {
      setLocationState({
        status: 'locating',
        text: '正在通过高德定位...',
      });

      try {
        location = await resolveAmapLocation();
        setLocationState({
          status: 'success',
          text: '高德定位成功',
          detail: formatLocationDetail(location),
          location,
        });
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : '高德定位失败';
        setLocationState({
          status: 'failed',
          text: errorMessage,
          detail: '将继续尝试提交打卡，由后端返回最终校验结果',
        });
        message.warning(`${errorMessage}，将继续尝试打卡`);
      }

      const response = await clockAttendance({
        type,
        latitude: location?.latitude,
        longitude: location?.longitude,
        deviceInfo: getDeviceInfo(location),
      });

      const result = normalizeClockResult(
        response as ClockResponsePayload,
        type,
      );
      const label = getClockLabel(result.period);
      const networkIp = getClockIp(result);
      const locationDetail = formatLocationDetail(location);

      Modal.success({
        title: `${label}成功`,
        content: (
          <div className={styles.successModalContent}>
            <p>打卡时间：{formatTime(result.clockTime as BackendDateValue)}</p>
            <p>打卡状态：{getStatusLabel(result.status)}</p>
            <p>打卡位置：{formatLocationText(location)}</p>
            {locationDetail && <p>{locationDetail}</p>}
            <p>网络 IP：{networkIp || '后端暂未返回'}</p>
          </div>
        ),
        okText: '我知道了',
      });
      refresh();
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : '打卡提交失败';
      Modal.error({
        title: '打卡提交失败',
        content: errorMessage,
        okText: '我知道了',
      });
      refresh();
    } finally {
      setClocking(false);
    }
  };

  useEffect(() => {
    const timer = window.setInterval(() => setNow(dayjs()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  const latestNetworkIp =
    todayRecord?.clockOutIp || todayRecord?.clockInIp;
  const clockInDone = Boolean(todayRecord?.clockInTime);
  const clockOutDone = Boolean(todayRecord?.clockOutTime);
  const nextClockType: ClockPeriod = clockInDone ? 'CLOCK_OUT' : 'CLOCK_IN';
  const nextClockText = clockInDone ? '下班打卡' : '上班打卡';
  const dayStatus =
    todayRecord?.dayStatus || (clockInDone || clockOutDone ? 'NORMAL' : undefined);

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
                {now.format('YYYY-MM-DD')} {now.format('dddd')}
              </div>
              <div className={styles.timeText}>{now.format('HH:mm')}</div>
              <div className={styles.groupLine}>
                所属考勤组：<span className={styles.groupName}>标准工时组</span>
              </div>

              <Row gutter={[24, 24]} className={styles.shiftCards}>
                <Col xs={24} md={12}>
                  <div
                    className={`${styles.shiftCard} ${
                      clockInDone ? styles.shiftDone : ''
                    }`}
                  >
                    <span
                      className={`${styles.shiftIcon} ${
                        clockInDone ? styles.shiftIconDone : ''
                      }`}
                    >
                      <ClockCircleOutlined />
                    </span>
                    <div>
                      <div className={styles.shiftLabel}>上班打卡</div>
                      <div
                        className={
                          clockInDone ? styles.shiftTime : styles.shiftPending
                        }
                      >
                        {formatTime(
                          todayRecord?.clockInTime as BackendDateValue,
                        )}
                      </div>
                      <Tag
                        color={
                          clockInDone
                            ? STATUS_COLOR_MAP[todayRecord?.clockInStatus || 'NORMAL']
                            : 'default'
                        }
                      >
                        {clockInDone
                          ? getStatusLabel(todayRecord?.clockInStatus || 'NORMAL')
                          : '待打卡'}
                      </Tag>
                    </div>
                  </div>
                </Col>
                <Col xs={24} md={12}>
                  <div
                    className={`${styles.shiftCard} ${
                      clockOutDone ? styles.shiftDone : ''
                    }`}
                  >
                    <span
                      className={`${styles.shiftIcon} ${
                        clockOutDone ? styles.shiftIconDone : ''
                      }`}
                    >
                      <ClockCircleOutlined />
                    </span>
                    <div>
                      <div className={styles.shiftLabel}>下班打卡</div>
                      <div
                        className={
                          clockOutDone ? styles.shiftTime : styles.shiftPending
                        }
                      >
                        {formatTime(
                          todayRecord?.clockOutTime as BackendDateValue,
                        )}
                      </div>
                      <Tag
                        color={
                          clockOutDone
                            ? STATUS_COLOR_MAP[todayRecord?.clockOutStatus || 'NORMAL']
                            : 'default'
                        }
                      >
                        {clockOutDone
                          ? getStatusLabel(todayRecord?.clockOutStatus || 'NORMAL')
                          : '待打卡'}
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
                onClick={() => handleSubmitClock(nextClockType)}
              >
                {clockInDone && clockOutDone ? '今日已完成打卡' : nextClockText}
              </Button>

              <div className={styles.ruleLine}>
                今日上班时间：09:00 | 规定下班时间：18:00
              </div>

              <div className={styles.footerStatus}>
                <Space>
                  <EnvironmentOutlined
                    style={{ color: '#1fbf63', fontSize: 22 }}
                  />
                  <span>
                    打卡位置：
                    <Tag
                      color={LOCATION_STATUS_COLOR[locationState.status]}
                      style={{ marginLeft: 8 }}
                    >
                      {locationState.text}
                    </Tag>
                    {locationState.location && (
                      <Text type="secondary">
                        {formatLocationText(locationState.location)}
                      </Text>
                    )}
                    {locationState.detail && (
                      <Text type="secondary">，{locationState.detail}</Text>
                    )}
                  </span>
                </Space>
                <Space>
                  <SafetyCertificateFilled
                    style={{ color: '#1fbf63', fontSize: 22 }}
                  />
                  网络 IP：{latestNetworkIp || '打卡成功后显示'}
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
              <Button
                type="link"
                onClick={() => history.push('/profile/attendance')}
              >
                查看月度记录 <RightOutlined />
              </Button>
            </div>

            <div className={styles.timeline}>
              <div className={styles.timelineItem}>
                <span
                  className={`${styles.timelineDot} ${
                    clockInDone ? styles.timelineDotDone : ''
                  }`}
                >
                  {clockInDone && <CheckCircleFilled />}
                </span>
                <div>
                  <div className={styles.recordTime}>
                    {formatTime(
                      todayRecord?.clockInTime as BackendDateValue,
                    )}
                  </div>
                  <div className={styles.recordMeta}>
                    上班打卡{' '}
                    <Tag
                      color={
                        clockInDone
                          ? STATUS_COLOR_MAP[todayRecord?.clockInStatus || 'NORMAL']
                          : 'default'
                      }
                    >
                      {clockInDone
                        ? getStatusLabel(todayRecord?.clockInStatus || 'NORMAL')
                        : '未打卡'}
                    </Tag>
                  </div>
                </div>
                <Text type="secondary">{todayRecord?.clockInIp || '--'}</Text>
              </div>

              <div className={styles.timelineItem}>
                <span
                  className={`${styles.timelineDot} ${
                    clockOutDone ? styles.timelineDotDone : ''
                  }`}
                >
                  {clockOutDone && <CheckCircleFilled />}
                </span>
                <div>
                  <div className={styles.recordTime}>
                    {formatTime(
                      todayRecord?.clockOutTime as BackendDateValue,
                    )}
                  </div>
                  <div className={styles.recordMeta}>
                    下班打卡{' '}
                    <Tag
                      color={
                        clockOutDone
                          ? STATUS_COLOR_MAP[todayRecord?.clockOutStatus || 'NORMAL']
                          : 'default'
                      }
                    >
                      {clockOutDone
                        ? getStatusLabel(todayRecord?.clockOutStatus || 'NORMAL')
                        : '未打卡'}
                    </Tag>
                  </div>
                </div>
                <Text type="secondary">{todayRecord?.clockOutIp || '--'}</Text>
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

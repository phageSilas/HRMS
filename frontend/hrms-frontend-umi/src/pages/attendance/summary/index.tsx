import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type {
  AttendanceDeptDistribution,
  AttendanceEmployeeRanking,
  AttendanceExceptionPie,
  AttendanceSummaryDashboard,
  AttendanceTrendPoint,
} from '@/services/attendance';
import { getAttendanceSummaryDashboard } from '@/services/attendance';
import type { Department } from '@/services/organization';
import { getDepartmentList } from '@/services/organization';
import type { UserInfo } from '@/types/user';
import {
  AreaChartOutlined,
  ApartmentOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  FallOutlined,
  ReloadOutlined,
  RiseOutlined,
  SearchOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { Column, Line, Pie } from '@ant-design/charts';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Row,
  Select,
  Space,
  Spin,
  Statistic,
  Table,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { type Dayjs } from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Text, Title } = Typography;

const SUMMARY_FILTER_STORAGE_PREFIX = 'attendance-summary-filter';
const ALL_DEPT_VALUE = '__ALL__';
const HR_ROLE_CODES = new Set(['HR', 'HR_TEST', 'ADMIN', 'ROLE_ADMIN']);

interface SummaryFilterState {
  yearMonth: string;
  deptId?: number;
}

interface UserContext extends UserInfo {
  roleCode: string;
}

interface SummaryCardConfig {
  key: string;
  title: string;
  value: number;
  color: string;
  icon: React.ReactNode;
  suffix: string;
  ratioLabel?: string;
  ratioValue?: number;
  momValue?: number | null;
  momDirection?: 'up' | 'down' | 'flat' | 'none';
}

interface SummaryMetricMeta {
  ratioLabel: string;
  ratioValue: number;
  momValue: number | null;
  momDirection: 'up' | 'down' | 'flat' | 'none';
}

const exceptionTypeLabelMap: Record<string, string> = {
  LATE: '迟到',
  EARLY_LEAVE: '早退',
  ABSENCE: '旷工',
};

const chartColorMap: Record<string, string> = {
  LATE: '#fa8c16',
  EARLY_LEAVE: '#1677ff',
  ABSENCE: '#ff4d4f',
};

function resolveSummaryFilterStorageKey() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return `${SUMMARY_FILTER_STORAGE_PREFIX}:anonymous`;
  }

  try {
    const userInfo = JSON.parse(userInfoText) as {
      userId?: number | string;
      id?: number | string;
      username?: string;
    };
    const identity = userInfo.userId || userInfo.id || userInfo.username || 'anonymous';
    return `${SUMMARY_FILTER_STORAGE_PREFIX}:${identity}`;
  } catch {
    return `${SUMMARY_FILTER_STORAGE_PREFIX}:anonymous`;
  }
}

function getStoredSummaryFilter(): Partial<SummaryFilterState> {
  const storageKey = resolveSummaryFilterStorageKey();
  const storedText = sessionStorage.getItem(storageKey);
  if (!storedText) {
    return {};
  }

  try {
    return JSON.parse(storedText) as Partial<SummaryFilterState>;
  } catch {
    sessionStorage.removeItem(storageKey);
    return {};
  }
}

function getCurrentUserFromStorage() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return undefined;
  }

  try {
    return JSON.parse(userInfoText) as UserContext;
  } catch {
    return undefined;
  }
}

function isHrView(currentUser?: UserContext) {
  return HR_ROLE_CODES.has(currentUser?.roleCode || '');
}

function toNumber(value?: number | string) {
  if (typeof value === 'number') {
    return value;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function safeDivide(numerator: number, denominator: number) {
  if (denominator <= 0) {
    return 0;
  }
  return numerator / denominator;
}

function toPercent(value?: number | null, digits = 1) {
  if (value == null || !Number.isFinite(value)) {
    return '--';
  }
  return `${(value * 100).toFixed(digits)}%`;
}

function calcMoM(current: number, previous: number) {
  if (previous === 0) {
    return current === 0 ? 0 : null;
  }
  return (current - previous) / previous;
}

function getMomDirection(value: number | null): SummaryMetricMeta['momDirection'] {
  if (value == null) {
    return 'none';
  }
  if (value > 0) {
    return 'up';
  }
  if (value < 0) {
    return 'down';
  }
  return 'flat';
}

function formatDateLabel(value: string | number[]) {
  if (Array.isArray(value)) {
    const [, month, day] = value;
    return `${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return dayjs(value).format('MM-DD');
}

function normalizeTrendData(dailyTrend: AttendanceTrendPoint[]) {
  return dailyTrend.map((item) => ({
    dateLabel: formatDateLabel(item.date),
    attendanceRate: toNumber(item.attendanceRate),
    actualDays: item.actualDays,
    expectedDays: item.expectedDays,
  }));
}

function normalizeDeptData(deptDistribution: AttendanceDeptDistribution[]) {
  return deptDistribution.map((item) => ({
    ...item,
    attendanceRate: toNumber(item.attendanceRate),
  }));
}

function normalizePieData(exceptionPie: AttendanceExceptionPie[]) {
  return exceptionPie.map((item) => ({
    type: exceptionTypeLabelMap[item.type] || item.type,
    count: item.count,
    rawType: item.type,
  }));
}

const AttendanceSummaryPage: React.FC = () => {
  const currentUser = getCurrentUserFromStorage();
  const isHr = isHrView(currentUser);
  const storedFilter = getStoredSummaryFilter();
  const [loading, setLoading] = useState(false);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [dashboard, setDashboard] = useState<AttendanceSummaryDashboard>();
  const [previousDashboard, setPreviousDashboard] = useState<AttendanceSummaryDashboard>();
  const [filter, setFilter] = useState<SummaryFilterState>({
    yearMonth: storedFilter.yearMonth || dayjs().format('YYYY-MM'),
    deptId: storedFilter.deptId ?? (isHr ? undefined : currentUser?.deptId),
  });

  const loadDepartments = async () => {
    setDepartmentLoading(true);
    try {
      const nextDepartments = await getDepartmentList();
      setDepartments(nextDepartments);
      return nextDepartments;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '部门列表加载失败';
      message.error(messageText);
      return departments;
    } finally {
      setDepartmentLoading(false);
    }
  };

  const loadDashboard = async (nextFilter: SummaryFilterState) => {
    setLoading(true);
    try {
      const previousYearMonth = dayjs(nextFilter.yearMonth, 'YYYY-MM')
        .subtract(1, 'month')
        .format('YYYY-MM');
      const [nextDashboard, nextPreviousDashboard] = await Promise.all([
        getAttendanceSummaryDashboard({
          yearMonth: nextFilter.yearMonth,
          deptId: nextFilter.deptId,
        }),
        getAttendanceSummaryDashboard({
          yearMonth: previousYearMonth,
          deptId: nextFilter.deptId,
        }).catch(() => undefined),
      ]);
      setDashboard(nextDashboard);
      setPreviousDashboard(nextPreviousDashboard);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '考勤统计加载失败';
      message.error(messageText);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDepartments();
  }, []);

  useEffect(() => {
    void loadDashboard(filter);
    const storageKey = resolveSummaryFilterStorageKey();
    sessionStorage.setItem(storageKey, JSON.stringify(filter));
  }, [filter]);

  useEffect(() => {
    if (isHr || !currentUser?.deptId) {
      return;
    }
    if (departments.length === 0) {
      return;
    }
    if (departments.some((item) => item.id === currentUser.deptId)) {
      return;
    }
    setFilter((previous) => ({
      ...previous,
      deptId: undefined,
    }));
  }, [currentUser?.deptId, departments, isHr]);

  usePageAutoRefresh(() => {
    void loadDepartments();
    void loadDashboard(filter);
  });

  const departmentOptions = useMemo(() => {
    const options = departments.map((department) => ({
      label: department.deptName,
      value: department.id,
    }));

    if (isHr) {
      return [{ label: '全部部门', value: ALL_DEPT_VALUE }, ...options];
    }
    return options;
  }, [departments, isHr]);

  const trendData = useMemo(
    () => normalizeTrendData(dashboard?.dailyTrend || []),
    [dashboard?.dailyTrend],
  );
  const deptData = useMemo(
    () => normalizeDeptData(dashboard?.deptDistribution || []),
    [dashboard?.deptDistribution],
  );
  const pieData = useMemo(
    () => normalizePieData(dashboard?.exceptionPie || []),
    [dashboard?.exceptionPie],
  );

  const metricMeta = useMemo(() => {
    const currentExpectedDays = dashboard?.expectedDays || 0;
    const previousExpectedDays = previousDashboard?.expectedDays || 0;

    const actualRate = safeDivide(dashboard?.actualDays || 0, currentExpectedDays);
    const previousActualRate = safeDivide(previousDashboard?.actualDays || 0, previousExpectedDays);
    const lateRate = safeDivide(dashboard?.lateCount || 0, currentExpectedDays);
    const previousLateRate = safeDivide(previousDashboard?.lateCount || 0, previousExpectedDays);
    const earlyLeaveRate = safeDivide(
      dashboard?.earlyLeaveCount || 0,
      currentExpectedDays,
    );
    const previousEarlyLeaveRate = safeDivide(
      previousDashboard?.earlyLeaveCount || 0,
      previousExpectedDays,
    );
    const absentRate = safeDivide(dashboard?.absentCount || 0, currentExpectedDays);
    const previousAbsentRate = safeDivide(
      previousDashboard?.absentCount || 0,
      previousExpectedDays,
    );
    const leaveRate = safeDivide(
      Number(dashboard?.leaveCount || 0),
      currentExpectedDays,
    );
    const previousLeaveRate = safeDivide(
      Number(previousDashboard?.leaveCount || 0),
      previousExpectedDays,
    );

    return {
      actualDays: {
        ratioLabel: '本月出勤率',
        ratioValue: actualRate,
        momValue: calcMoM(actualRate, previousActualRate),
        momDirection: getMomDirection(calcMoM(actualRate, previousActualRate)),
      },
      lateCount: {
        ratioLabel: '本月占比',
        ratioValue: lateRate,
        momValue: calcMoM(lateRate, previousLateRate),
        momDirection: getMomDirection(calcMoM(lateRate, previousLateRate)),
      },
      earlyLeaveCount: {
        ratioLabel: '本月占比',
        ratioValue: earlyLeaveRate,
        momValue: calcMoM(earlyLeaveRate, previousEarlyLeaveRate),
        momDirection: getMomDirection(calcMoM(earlyLeaveRate, previousEarlyLeaveRate)),
      },
      absentCount: {
        ratioLabel: '本月占比',
        ratioValue: absentRate,
        momValue: calcMoM(absentRate, previousAbsentRate),
        momDirection: getMomDirection(calcMoM(absentRate, previousAbsentRate)),
      },
      leaveCount: {
        ratioLabel: '本月占比',
        ratioValue: leaveRate,
        momValue: calcMoM(leaveRate, previousLeaveRate),
        momDirection: getMomDirection(calcMoM(leaveRate, previousLeaveRate)),
      },
    } satisfies Record<string, SummaryMetricMeta>;
  }, [dashboard, previousDashboard]);

  const rankingColumns: ColumnsType<AttendanceEmployeeRanking> = [
    {
      title: '排名',
      key: 'rank',
      width: 76,
      render: (_value, _record, index) => {
        const rank = index + 1;
        const color =
          rank === 1 ? '#d48806' : rank === 2 ? '#595959' : rank === 3 ? '#ad4e00' : '#8c8c8c';
        return <Text style={{ color, fontWeight: 700 }}>#{rank}</Text>;
      },
    },
    {
      title: '员工',
      key: 'employeeName',
      render: (_value, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.employeeName}</Text>
          <Text type="secondary">{record.employeeNo}</Text>
        </Space>
      ),
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 160,
    },
    {
      title: '异常总数',
      dataIndex: 'abnormalCount',
      width: 110,
      render: (value) => <Text strong style={{ color: '#cf1322' }}>{value}</Text>,
    },
    {
      title: '迟到',
      dataIndex: 'lateCount',
      width: 90,
    },
    {
      title: '早退',
      dataIndex: 'earlyLeaveCount',
      width: 90,
    },
    {
      title: '旷工',
      dataIndex: 'absentCount',
      width: 90,
    },
  ];

  const summaryCards: SummaryCardConfig[] = [
    {
      key: 'expectedDays',
      title: '应出勤',
      value: dashboard?.expectedDays || 0,
      color: '#1677ff',
      icon: <CalendarCardIcon />,
      suffix: '人天',
    },
    {
      key: 'actualDays',
      title: '实际出勤',
      value: dashboard?.actualDays || 0,
      color: '#13c2c2',
      icon: <TeamOutlined />,
      suffix: '人天',
      ...metricMeta.actualDays,
    },
    {
      key: 'lateCount',
      title: '迟到',
      value: dashboard?.lateCount || 0,
      color: '#fa8c16',
      icon: <ClockCircleOutlined />,
      suffix: '次',
      ...metricMeta.lateCount,
    },
    {
      key: 'earlyLeaveCount',
      title: '早退',
      value: dashboard?.earlyLeaveCount || 0,
      color: '#722ed1',
      icon: <FallOutlined />,
      suffix: '次',
      ...metricMeta.earlyLeaveCount,
    },
    {
      key: 'absentCount',
      title: '旷工',
      value: dashboard?.absentCount || 0,
      color: '#f5222d',
      icon: <AreaChartOutlined />,
      suffix: '次',
      ...metricMeta.absentCount,
    },
    {
      key: 'leaveCount',
      title: '请假',
      value: Number(dashboard?.leaveCount || 0),
      color: '#52c41a',
      icon: <DashboardOutlined />,
      suffix: '天',
      ...metricMeta.leaveCount,
    },
  ];

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>考勤统计</Title>
          <Text type="secondary">面向 HR 和部门主管的部门级考勤看板，支持按月份和部门查看整体趋势与异常排行。</Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} md={8} xl={6}>
            <Space direction="vertical" size={6} style={{ width: '100%' }}>
              <Text type="secondary">月份</Text>
              <DatePicker
                picker="month"
                allowClear={false}
                value={dayjs(filter.yearMonth, 'YYYY-MM')}
                onChange={(value: Dayjs | null) => {
                  if (!value) return;
                  setFilter((previous) => ({
                    ...previous,
                    yearMonth: value.format('YYYY-MM'),
                  }));
                }}
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          <Col xs={24} md={8} xl={6}>
            <Space direction="vertical" size={6} style={{ width: '100%' }}>
              <Text type="secondary">部门</Text>
              <Select
                showSearch
                allowClear={isHr}
                loading={departmentLoading}
                optionFilterProp="label"
                placeholder="请选择部门"
                value={isHr && filter.deptId == null ? ALL_DEPT_VALUE : filter.deptId}
                options={departmentOptions}
                onChange={(value) => {
                  setFilter((previous) => ({
                    ...previous,
                    deptId:
                      value === ALL_DEPT_VALUE || value == null
                        ? undefined
                        : Number(value),
                  }));
                }}
              />
            </Space>
          </Col>
          <Col xs={24} xl={12}>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={() => void loadDashboard(filter)}
              >
                查询
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={() =>
                  setFilter({
                    yearMonth: dayjs().format('YYYY-MM'),
                    deptId: isHr ? undefined : currentUser?.deptId,
                  })
                }
              >
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
          {summaryCards.map((card) => (
            <Col xs={24} sm={12} xl={8} xxl={4} key={card.key}>
              <Card bordered={false} style={{ borderRadius: 20, minHeight: 148 }}>
                <Space align="start" size={16}>
                  <div
                    style={{
                      width: 52,
                      height: 52,
                      borderRadius: 16,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      background: `${card.color}14`,
                      color: card.color,
                      fontSize: 24,
                    }}
                  >
                    {card.icon}
                  </div>
                  <Space direction="vertical" size={4}>
                    <Text type="secondary">{card.title}</Text>
                    <Statistic value={card.value} suffix={card.suffix} />
                    {card.ratioLabel && (
                      <Text type="secondary">
                        {card.ratioLabel} {toPercent(card.ratioValue)}
                      </Text>
                    )}
                    {card.ratioLabel && (
                      <Space size={4}>
                        <Text type="secondary">较上月</Text>
                        {card.momDirection === 'up' && <RiseOutlined style={{ color: '#52c41a' }} />}
                        {card.momDirection === 'down' && <FallOutlined style={{ color: '#ff4d4f' }} />}
                        <Text
                          style={{
                            color:
                              card.momDirection === 'up'
                                ? '#52c41a'
                                : card.momDirection === 'down'
                                ? '#ff4d4f'
                                : '#8c8c8c',
                          }}
                        >
                          {toPercent(card.momValue)}
                        </Text>
                      </Space>
                    )}
                  </Space>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>

        <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
          <Col xs={24} xl={11}>
            <Card
              bordered={false}
              style={{ borderRadius: 20, height: '100%' }}
              title="考勤趋势"
              extra={<Text type="secondary">按日统计</Text>}
            >
              {trendData.length > 0 ? (
                <Line
                  height={280}
                  data={trendData}
                  xField="dateLabel"
                  yField="attendanceRate"
                  point={{ size: 4, shape: 'circle' }}
                  color="#1677ff"
                  yAxis={{ label: { formatter: (value: string) => `${value}%` } }}
                  tooltip={{
                    items: [
                      (datum: { attendanceRate: number; actualDays: number; expectedDays: number }) => ({
                        name: '出勤率',
                        value: `${datum.attendanceRate.toFixed(2)}%`,
                      }),
                      (datum: { actualDays: number }) => ({
                        name: '实际出勤',
                        value: `${datum.actualDays}`,
                      }),
                      (datum: { expectedDays: number }) => ({
                        name: '应出勤',
                        value: `${datum.expectedDays}`,
                      }),
                    ],
                  }}
                  smooth
                />
              ) : (
                <Empty description="暂无趋势数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Col>
          <Col xs={24} xl={6}>
            <Card
              bordered={false}
              style={{ borderRadius: 20, height: '100%' }}
              title="异常类型分布"
              extra={<Text type="secondary">本月</Text>}
            >
              {pieData.length > 0 ? (
                <Pie
                  height={280}
                  data={pieData}
                  angleField="count"
                  colorField="type"
                  color={({ rawType }: { rawType: string }) => chartColorMap[rawType] || '#1677ff'}
                  label={{ text: 'count', style: { fontWeight: 600 } }}
                  legend={{ color: { title: false, position: 'right' } }}
                  tooltip={{
                    items: [
                      (datum: { type: string; count: number }) => ({
                        name: datum.type,
                        value: `${datum.count} 次`,
                      }),
                    ],
                  }}
                />
              ) : (
                <Empty description="暂无异常分布数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Col>
          <Col xs={24} xl={7}>
            <Card
              bordered={false}
              style={{ borderRadius: 20, height: '100%' }}
              title="部门分布"
              extra={<Text type="secondary">{isHr ? '全部部门' : '当前范围'}</Text>}
            >
              {deptData.length > 0 ? (
                <Column
                  height={280}
                  data={deptData}
                  xField="deptName"
                  yField="attendanceRate"
                  color="#13c2c2"
                  xAxis={{ label: { autoRotate: true } }}
                  yAxis={{ label: { formatter: (value: string) => `${value}%` } }}
                  tooltip={{
                    items: [
                      (datum: { attendanceRate: number; actualDays: number; expectedDays: number }) => ({
                        name: '出勤率',
                        value: `${datum.attendanceRate.toFixed(2)}%`,
                      }),
                      (datum: { actualDays: number }) => ({
                        name: '实际出勤',
                        value: `${datum.actualDays}`,
                      }),
                      (datum: { expectedDays: number }) => ({
                        name: '应出勤',
                        value: `${datum.expectedDays}`,
                      }),
                    ],
                  }}
                />
              ) : (
                <Empty description="暂无部门分布数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Col>
        </Row>

        <Card
          bordered={false}
          style={{ borderRadius: 20 }}
          title="个人异常排名"
          extra={<Text type="secondary">按异常总次数排序，默认展示前 10 名</Text>}
        >
          <Table<AttendanceEmployeeRanking>
            rowKey="employeeId"
            columns={rankingColumns}
            dataSource={dashboard?.employeeRanking || []}
            pagination={false}
            locale={{ emptyText: '暂无员工异常排名数据' }}
            scroll={{ x: 820 }}
          />
        </Card>
      </Spin>
    </PageContainer>
  );
};

const CalendarCardIcon: React.FC = () => <ApartmentOutlined />;

export default AttendanceSummaryPage;

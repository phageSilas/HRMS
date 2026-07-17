import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type { Department } from '@/services/organization';
import { getDepartmentList } from '@/services/organization';
import type {
  AttendanceGroup,
  AttendanceGroupRecord,
  AttendanceGroupRecordQuery,
} from '@/services/attendance';
import {
  getAttendanceGroupRecords,
  getAttendanceGroups,
} from '@/services/attendance';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import type { PageResult } from '@/types/api';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

const { RangePicker } = DatePicker;
const { Text, Title } = Typography;

interface RecordFilterValues {
  groupId?: number;
  yearMonth?: Dayjs;
  dateRange?: [Dayjs, Dayjs];
  keyword?: string;
  departmentId?: number;
  status?: string;
}

interface RecordQueryState {
  groupId?: number;
  yearMonth: string;
  dateStart?: string;
  dateEnd?: string;
  keyword?: string;
  departmentId?: number;
  status?: string;
  pageNum: number;
  pageSize: number;
}

interface StoredRecordQueryState extends Partial<RecordQueryState> {}

const statusMeta: Record<string, { label: string; color: string; desc: string }> = {
  NORMAL: { label: '正常', color: 'success', desc: '上下班均正常' },
  LATE: { label: '迟到', color: 'warning', desc: '上班状态为迟到' },
  EARLY_LEAVE: { label: '早退', color: 'orange', desc: '下班状态为早退' },
  ABSENCE: { label: '缺勤', color: 'error', desc: '当天上下班均无记录' },
  CLOCK_IN_MISSING: { label: '上班缺卡', color: 'purple', desc: '下班有记录，上班无记录' },
  CLOCK_OUT_MISSING: { label: '下班缺卡', color: 'blue', desc: '上班有记录，下班无记录' },
  ABNORMAL: { label: '异常', color: 'red', desc: '同时迟到和早退' },
};

const statusOptions = Object.entries(statusMeta).map(([value, meta]) => ({
  label: meta.label,
  value,
}));

const RECORD_QUERY_STORAGE_PREFIX = 'attendance-record-query';
const DEPARTMENT_PAGE_SIZE = 200;

function parseUrlGroupId() {
  const groupId = new URLSearchParams(history.location.search).get('groupId');
  const parsed = Number(groupId);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
}

function resolveRecordQueryStorageKey() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return `${RECORD_QUERY_STORAGE_PREFIX}:anonymous`;
  }

  try {
    const userInfo = JSON.parse(userInfoText) as {
      userId?: number | string;
      id?: number | string;
      username?: string;
    };
    const identity = userInfo.userId || userInfo.id || userInfo.username || 'anonymous';
    return `${RECORD_QUERY_STORAGE_PREFIX}:${identity}`;
  } catch {
    return `${RECORD_QUERY_STORAGE_PREFIX}:anonymous`;
  }
}

function getStoredRecordQuery(): StoredRecordQueryState {
  const storageKey = resolveRecordQueryStorageKey();
  const storedText = sessionStorage.getItem(storageKey);
  if (!storedText) {
    return {};
  }

  try {
    return JSON.parse(storedText) as StoredRecordQueryState;
  } catch {
    sessionStorage.removeItem(storageKey);
    return {};
  }
}

function formatBackendDate(value?: string | number[]) {
  if (!value) return '--';
  if (Array.isArray(value)) {
    const [year, month, day] = value;
    if (!year || !month || !day) return '--';
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return value;
}

function formatBackendTime(value?: string | number[]) {
  if (!value) return '--';
  if (Array.isArray(value)) {
    const [hour = 0, minute = 0, second = 0] = value;
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`;
  }
  return value;
}

function renderStatusTag(value?: string, fallbackName?: string) {
  if (!value && !fallbackName) return <Text type="secondary">--</Text>;
  const meta = value ? statusMeta[value] : undefined;
  return <Tag color={meta?.color || 'default'}>{fallbackName || meta?.label || value}</Tag>;
}

function buildRecordQuery(query: RecordQueryState): AttendanceGroupRecordQuery {
  const params: AttendanceGroupRecordQuery = {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword,
    departmentId: query.departmentId,
    status: query.status,
  };

  if (query.dateStart && query.dateEnd) {
    params.dateStart = query.dateStart;
    params.dateEnd = query.dateEnd;
  } else {
    params.yearMonth = query.yearMonth;
  }

  return params;
}

const AttendanceRecordPage: React.FC = () => {
  const [form] = Form.useForm<RecordFilterValues>();
  const storedQuery = getStoredRecordQuery();
  const [groups, setGroups] = useState<AttendanceGroup[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [groupLoading, setGroupLoading] = useState(false);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [recordLoading, setRecordLoading] = useState(false);
  const [recordPageData, setRecordPageData] = useState<
    PageResult<AttendanceGroupRecord>
  >();
  const [query, setQuery] = useState<RecordQueryState>({
    groupId: parseUrlGroupId() ?? storedQuery.groupId,
    yearMonth: storedQuery.yearMonth || dayjs().format('YYYY-MM'),
    dateStart: storedQuery.dateStart,
    dateEnd: storedQuery.dateEnd,
    keyword: storedQuery.keyword,
    departmentId: storedQuery.departmentId,
    status: storedQuery.status,
    pageNum: storedQuery.pageNum || 1,
    pageSize: storedQuery.pageSize || 10,
  });
  const departmentOptions = useMemo(
    () =>
      departments.map((department) => ({
        label: department.deptName,
        value: department.id,
      })),
    [departments],
  );

  const loadGroups = async () => {
    setGroupLoading(true);
    try {
      const page = await getAttendanceGroups({ pageNum: 1, pageSize: 100 });
      const nextGroups = page.records ?? [];
      setGroups(nextGroups);
      return nextGroups;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '考勤组加载失败';
      message.error(messageText);
      return groups;
    } finally {
      setGroupLoading(false);
    }
  };

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

  const loadRecords = async (nextQuery: RecordQueryState) => {
    if (!nextQuery.groupId) {
      setRecordPageData({
        records: [],
        total: 0,
        pageNum: nextQuery.pageNum,
        pageSize: nextQuery.pageSize,
      });
      return;
    }

    setRecordLoading(true);
    try {
      const nextPageData = await getAttendanceGroupRecords(
        nextQuery.groupId,
        buildRecordQuery(nextQuery),
      );
      setRecordPageData(nextPageData);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '考勤记录加载失败';
      message.error(messageText);
    } finally {
      setRecordLoading(false);
    }
  };

  useEffect(() => {
    void loadGroups();
    void loadDepartments();
  }, []);

  useEffect(() => {
    if (groups.length === 0) return;
    if (query.groupId && groups.some((item) => item.id === query.groupId)) return;
    const firstGroupId = groups[0].id;
    setQuery((previous) => ({ ...previous, groupId: firstGroupId, pageNum: 1 }));
  }, [groups, query.groupId]);

  useEffect(() => {
    if (!query.departmentId) return;
    if (departments.some((item) => item.id === query.departmentId)) return;
    setQuery((previous) => ({
      ...previous,
      departmentId: undefined,
      pageNum: 1,
    }));
  }, [departments, query.departmentId]);

  useEffect(() => {
    form.setFieldsValue({
      groupId: query.groupId,
      yearMonth: dayjs(query.yearMonth, 'YYYY-MM'),
      dateRange:
        query.dateStart && query.dateEnd
          ? [dayjs(query.dateStart, 'YYYY-MM-DD'), dayjs(query.dateEnd, 'YYYY-MM-DD')]
          : undefined,
      keyword: query.keyword,
      departmentId: query.departmentId,
      status: query.status,
    });
  }, [
    form,
    query.dateEnd,
    query.dateStart,
    query.departmentId,
    query.groupId,
    query.keyword,
    query.status,
    query.yearMonth,
  ]);

  useEffect(() => {
    void loadRecords(query);
  }, [query]);

  useEffect(() => {
    const storageKey = resolveRecordQueryStorageKey();
    sessionStorage.setItem(storageKey, JSON.stringify(query));
  }, [query]);

  usePageAutoRefresh(() => {
    void (async () => {
      const nextGroups = await loadGroups();
      const nextDepartments = await loadDepartments();
      const nextGroupId =
        query.groupId && nextGroups.some((item) => item.id === query.groupId)
          ? query.groupId
          : nextGroups[0]?.id;
      const nextDepartmentId =
        query.departmentId &&
        nextDepartments.some((item) => item.id === query.departmentId)
          ? query.departmentId
          : undefined;

      if (!nextGroupId) {
        return;
      }

      if (
        nextGroupId !== query.groupId ||
        nextDepartmentId !== query.departmentId
      ) {
        setQuery((previous) => ({
          ...previous,
          groupId: nextGroupId,
          departmentId: nextDepartmentId,
          pageNum: 1,
        }));
        return;
      }

      await loadRecords({
        ...query,
        groupId: nextGroupId,
        departmentId: nextDepartmentId,
      });
    })();
  });

  const selectedGroup = groups.find((item) => item.id === query.groupId);

  const columns: ColumnsType<AttendanceGroupRecord> = [
    {
      title: '日期',
      dataIndex: 'recordDate',
      width: 130,
      render: (value) => formatBackendDate(value),
    },
    {
      title: '员工',
      dataIndex: 'employeeName',
      width: 160,
      render: (value, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{value || '--'}</Text>
          <Text type="secondary">{record.employeeNo || '--'}</Text>
        </Space>
      ),
    },
    { title: '工号', dataIndex: 'employeeNo', width: 120, render: (value) => value || '--' },
    { title: '部门', dataIndex: 'deptName', width: 150, render: (value) => value || '--' },
    {
      title: '上班时间',
      dataIndex: 'clockInTime',
      width: 130,
      render: (value) => formatBackendTime(value),
    },
    {
      title: '下班时间',
      dataIndex: 'clockOutTime',
      width: 130,
      render: (value) => formatBackendTime(value),
    },
    {
      title: '上班状态',
      dataIndex: 'clockInStatus',
      width: 120,
      render: (value) => renderStatusTag(value),
    },
    {
      title: '下班状态',
      dataIndex: 'clockOutStatus',
      width: 120,
      render: (value) => renderStatusTag(value),
    },
    {
      title: '综合状态',
      dataIndex: 'status',
      width: 120,
      fixed: 'right',
      render: (value, record) => renderStatusTag(value, record.statusName),
    },
  ];

  const handleSearch = (values: RecordFilterValues) => {
    const dateRange = values.dateRange;
    setQuery((previous) => ({
      ...previous,
      groupId: values.groupId,
      yearMonth: values.yearMonth?.format('YYYY-MM') || dayjs().format('YYYY-MM'),
      dateStart: dateRange?.[0]?.format('YYYY-MM-DD'),
      dateEnd: dateRange?.[1]?.format('YYYY-MM-DD'),
      keyword: values.keyword?.trim() || undefined,
      departmentId: values.departmentId,
      status: values.status,
      pageNum: 1,
    }));
  };

  const handleReset = () => {
    const nextGroupId = query.groupId || groups[0]?.id;
    setQuery({
      groupId: nextGroupId,
      yearMonth: dayjs().format('YYYY-MM'),
      departmentId: undefined,
      pageNum: 1,
      pageSize: query.pageSize,
    });
  };

  return (
    <PageContainer title={false} className={styles.recordPage}>
      <div className={styles.pageHeader}>
        <div>
          <Title level={3}>考勤记录</Title>
          <Text type="secondary">
            面向 HR、部门主管和管理员，按考勤组查询员工每日打卡明细
          </Text>
        </div>
        <Button type="primary" onClick={() => history.push('/profile/attendance')}>
          申请补卡
        </Button>
      </div>

      <Card bordered={false} className={styles.statusCard}>
        <Title level={5}>打卡状态说明</Title>
        <div className={styles.statusGrid}>
          {Object.entries(statusMeta).map(([key, item]) => (
            <div className={styles.statusItem} key={key}>
              <Tag color={item.color}>{item.label}</Tag>
              <Text type="secondary">{item.desc}</Text>
            </div>
          ))}
        </div>
      </Card>

      <Card bordered={false} className={styles.filterCard}>
        <Form
          form={form}
          layout="inline"
          className={styles.filterForm}
          initialValues={{
            groupId: query.groupId,
            yearMonth: dayjs(query.yearMonth),
          }}
          onFinish={handleSearch}
        >
          <Form.Item
            label="考勤组"
            name="groupId"
            rules={[{ required: true, message: '请选择考勤组' }]}
          >
            <Select
              showSearch
              loading={groupLoading}
              placeholder="请选择考勤组"
              optionFilterProp="label"
              className={styles.groupSelect}
              options={groups.map((group) => ({
                label: group.groupName,
                value: group.id,
              }))}
            />
          </Form.Item>
          <Form.Item label="月份" name="yearMonth">
            <DatePicker picker="month" allowClear={false} />
          </Form.Item>
          <Form.Item label="日期范围" name="dateRange">
            <RangePicker />
          </Form.Item>
          <Form.Item label="员工" name="keyword">
            <Input allowClear placeholder="姓名/工号" />
          </Form.Item>
          <Form.Item label="部门" name="departmentId">
            <Select
              showSearch
              allowClear
              loading={departmentLoading}
              placeholder="请选择部门"
              optionFilterProp="label"
              options={departmentOptions}
            />
          </Form.Item>
          <Form.Item label="综合状态" name="status">
            <Select
              allowClear
              placeholder="全部状态"
              className={styles.statusSelect}
              options={statusOptions}
            />
          </Form.Item>
          <Form.Item className={styles.filterActions}>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        bordered={false}
        className={styles.tableCard}
        title={
          <Space>
            <CalendarOutlined />
            <span>
              {selectedGroup?.groupName || '考勤组'} ·{' '}
              {query.dateStart && query.dateEnd
                ? `${query.dateStart} 至 ${query.dateEnd}`
                : `${query.yearMonth} 打卡记录`}
            </span>
          </Space>
        }
        extra={
          <Space className={styles.tableHint}>
            <ClockCircleOutlined />
            <Text type="secondary">最多支持 31 天日期范围查询</Text>
          </Space>
        }
      >
        <Table<AttendanceGroupRecord>
          rowKey={(record) =>
            String(record.recordId || `${record.employeeId}-${formatBackendDate(record.recordDate)}`)
          }
          columns={columns}
          dataSource={recordPageData?.records || []}
          loading={recordLoading || groupLoading}
          scroll={{ x: 1180 }}
          pagination={{
            current: recordPageData?.pageNum || query.pageNum,
            pageSize: recordPageData?.pageSize || query.pageSize,
            total: recordPageData?.total || 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (pageNum, pageSize) => {
              setQuery((previous) => ({
                ...previous,
                pageNum,
                pageSize,
              }));
            },
          }}
        />
      </Card>
    </PageContainer>
  );
};

export default AttendanceRecordPage;

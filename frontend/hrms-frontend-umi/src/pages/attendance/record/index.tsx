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
import { history, useRequest } from '@umijs/max';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
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

type AttendanceGroupPageLike =
  | {
      records?: AttendanceGroup[];
      data?: { records?: AttendanceGroup[] } | AttendanceGroup[];
    }
  | AttendanceGroup[]
  | undefined;

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

function normalizeGroups(pageData: AttendanceGroupPageLike) {
  if (Array.isArray(pageData)) return pageData;
  if (Array.isArray(pageData?.records)) return pageData.records;
  if (Array.isArray(pageData?.data)) return pageData.data;
  if (Array.isArray(pageData?.data?.records)) return pageData.data.records;
  return [];
}

function parseUrlGroupId() {
  const groupId = new URLSearchParams(history.location.search).get('groupId');
  const parsed = Number(groupId);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
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
  const [query, setQuery] = useState<RecordQueryState>({
    groupId: parseUrlGroupId(),
    yearMonth: dayjs().format('YYYY-MM'),
    pageNum: 1,
    pageSize: 10,
  });

  const { data: groupData, loading: groupLoading } = useRequest(
    () => getAttendanceGroups({ pageNum: 1, pageSize: 100 }),
    {
      onError: (error) => {
        message.error(error.message || '考勤组加载失败');
      },
    },
  );

  const groups = useMemo(() => {
    return normalizeGroups(groupData as AttendanceGroupPageLike);
  }, [groupData]);

  useEffect(() => {
    if (query.groupId || groups.length === 0) return;
    const firstGroupId = groups[0].id;
    setQuery((previous) => ({ ...previous, groupId: firstGroupId }));
    form.setFieldsValue({ groupId: firstGroupId });
  }, [form, groups, query.groupId]);

  useEffect(() => {
    if (!query.groupId) return;
    form.setFieldsValue({
      groupId: query.groupId,
      yearMonth: dayjs(query.yearMonth),
    });
  }, [form, query.groupId, query.yearMonth]);

  const { data: recordData, loading: recordLoading } = useRequest(
    () => {
      if (!query.groupId) {
        return Promise.resolve({
          records: [],
          total: 0,
          pageNum: query.pageNum,
          pageSize: query.pageSize,
        });
      }
      return getAttendanceGroupRecords(query.groupId, buildRecordQuery(query));
    },
    {
      refreshDeps: [query],
      onError: (error) => {
        message.error(error.message || '考勤记录加载失败');
      },
    },
  );

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
    form.setFieldsValue({
      groupId: nextGroupId,
      yearMonth: dayjs(),
      dateRange: undefined,
      keyword: undefined,
      departmentId: undefined,
      status: undefined,
    });
    setQuery({
      groupId: nextGroupId,
      yearMonth: dayjs().format('YYYY-MM'),
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
          <Form.Item label="部门ID" name="departmentId">
            <InputNumber min={1} precision={0} placeholder="部门ID" />
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
          dataSource={recordData?.records || []}
          loading={recordLoading || groupLoading}
          scroll={{ x: 1180 }}
          pagination={{
            current: recordData?.pageNum || query.pageNum,
            pageSize: recordData?.pageSize || query.pageSize,
            total: recordData?.total || 0,
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

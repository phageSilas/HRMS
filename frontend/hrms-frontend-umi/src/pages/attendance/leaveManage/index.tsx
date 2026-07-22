import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type {
  AttendanceLeaveManageItem,
  AttendanceLeaveManageQuery,
} from '@/services/attendance';
import {
  getAttendanceLeaveManageList,
  quickApproveAttendanceLeave,
} from '@/services/attendance';
import type { Department } from '@/services/organization';
import { getDepartmentList } from '@/services/organization';
import type { UserInfo } from '@/types/user';
import {
  EyeOutlined,
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
  Popconfirm,
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
import React, { useEffect, useMemo, useRef, useState } from 'react';

const { Text, Title } = Typography;

const LEAVE_MANAGE_STORAGE_PREFIX = 'attendance-leave-manage-query';
const HR_ROLE_CODES = new Set(['HR', 'HR_TEST', 'ADMIN', 'ROLE_ADMIN']);
const DEFAULT_PAGE_SIZE = 10;
const DEFAULT_APPROVAL_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '审批中', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已拒绝', value: 3 },
  { label: '已撤回', value: 4 },
];

interface LeaveManageFilterValues {
  yearMonth?: Dayjs;
  deptId?: number;
  keyword?: string;
  approvalStatus?: number;
}

interface LeaveManageQueryState {
  yearMonth: string;
  deptId?: number;
  keyword?: string;
  approvalStatus?: number;
  pageNum: number;
  pageSize: number;
}

interface UserContext extends UserInfo {
  roleCode: string;
}

const approvalStatusMeta: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'default' },
  1: { label: '审批中', color: 'processing' },
  2: { label: '已通过', color: 'success' },
  3: { label: '已拒绝', color: 'error' },
  4: { label: '已撤回', color: 'warning' },
};

/**
 * 从本地缓存读取当前用户信息，用于权限判断和缓存隔离。
 */
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

/**
 * 生成请假管理缓存键。
 */
function resolveStorageKey() {
  const currentUser = getCurrentUserFromStorage();
  const identity =
    currentUser?.userId || currentUser?.username || currentUser?.nickname || 'anonymous';
  return `${LEAVE_MANAGE_STORAGE_PREFIX}:${identity}`;
}

/**
 * 读取上次查询条件。
 */
function getStoredQuery() {
  const storedText = sessionStorage.getItem(resolveStorageKey());
  if (!storedText) {
    return {};
  }

  try {
    return JSON.parse(storedText) as Partial<LeaveManageQueryState>;
  } catch {
    sessionStorage.removeItem(resolveStorageKey());
    return {};
  }
}

/**
 * 判断是否为 HR 或管理员视角。
 */
function isHrOrAdmin(currentUser?: UserContext) {
  return HR_ROLE_CODES.has(currentUser?.roleCode || '');
}

/**
 * 判断是否为主管角色。
 */
function isManager(currentUser?: UserContext) {
  return currentUser?.roleCode === 'MANAGER';
}

/**
 * 格式化时间显示。
 */
function formatDateTime(value?: string) {
  if (!value) {
    return '--';
  }
  const date = dayjs(value);
  if (!date.isValid()) {
    return value;
  }
  return date.format('YYYY-MM-DD HH:mm');
}

/**
 * 渲染审批状态标签。
 */
function renderApprovalStatusTag(status?: number, label?: string) {
  if (status == null && !label) {
    return <Text type="secondary">--</Text>;
  }
  const meta = status != null ? approvalStatusMeta[status] : undefined;
  return <Tag color={meta?.color || 'default'}>{label || meta?.label || '--'}</Tag>;
}

/**
 * 构造请假管理查询参数。
 */
function buildLeaveManageParams(
  query: LeaveManageQueryState,
  refreshCache?: boolean,
): AttendanceLeaveManageQuery {
  return {
    yearMonth: query.yearMonth,
    deptId: query.deptId,
    keyword: query.keyword,
    approvalStatus: query.approvalStatus,
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    refreshCache,
  };
}

const AttendanceLeaveManagePage: React.FC = () => {
  const currentUser = getCurrentUserFromStorage();
  const canSelectDepartment = isHrOrAdmin(currentUser);
  const storedQuery = getStoredQuery();
  const [form] = Form.useForm<LeaveManageFilterValues>();
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [leaveLoading, setLeaveLoading] = useState(false);
  const [leavePageData, setLeavePageData] = useState<PageResult<AttendanceLeaveManageItem>>();
  const refreshCacheOnNextLoadRef = useRef(false);
  const [query, setQuery] = useState<LeaveManageQueryState>({
    yearMonth: storedQuery.yearMonth || dayjs().format('YYYY-MM'),
    deptId:
      storedQuery.deptId ??
      (isManager(currentUser) || !canSelectDepartment ? currentUser?.deptId : undefined),
    keyword: storedQuery.keyword,
    approvalStatus: storedQuery.approvalStatus,
    pageNum: storedQuery.pageNum || 1,
    pageSize: storedQuery.pageSize || DEFAULT_PAGE_SIZE,
  });

  const departmentOptions = useMemo(
    () =>
      departments.map((item) => ({
        label: item.deptName,
        value: item.id,
      })),
    [departments],
  );

  /**
   * 加载部门列表。
   */
  const loadDepartments = async () => {
    if (!canSelectDepartment) {
      return departments;
    }

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

  /**
   * 加载请假管理列表。
   */
  const loadLeaveManageList = async (
    nextQuery: LeaveManageQueryState,
    options?: { refreshCache?: boolean },
  ) => {
    setLeaveLoading(true);
    try {
      const nextPageData = await getAttendanceLeaveManageList(
        buildLeaveManageParams(nextQuery, options?.refreshCache),
      );
      setLeavePageData(nextPageData);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '请假管理列表加载失败';
      message.error(messageText);
    } finally {
      setLeaveLoading(false);
    }
  };

  useEffect(() => {
    if (canSelectDepartment) {
      void loadDepartments();
    }
  }, [canSelectDepartment]);

  useEffect(() => {
    form.setFieldsValue({
      yearMonth: dayjs(query.yearMonth, 'YYYY-MM'),
      deptId: query.deptId,
      keyword: query.keyword,
      approvalStatus: query.approvalStatus,
    });
  }, [form, query.approvalStatus, query.deptId, query.keyword, query.yearMonth]);

  useEffect(() => {
    const shouldRefreshCache = refreshCacheOnNextLoadRef.current;
    refreshCacheOnNextLoadRef.current = false;
    void loadLeaveManageList(query, { refreshCache: shouldRefreshCache });
  }, [query]);

  useEffect(() => {
    sessionStorage.setItem(resolveStorageKey(), JSON.stringify(query));
  }, [query]);

  useEffect(() => {
    if (!canSelectDepartment || !query.deptId) {
      return;
    }
    if (departments.some((item) => item.id === query.deptId)) {
      return;
    }
    setQuery((previous) => ({
      ...previous,
      deptId: undefined,
      pageNum: 1,
    }));
  }, [canSelectDepartment, departments, query.deptId]);

  usePageAutoRefresh(() => {
    void (async () => {
      const nextDepartments = canSelectDepartment ? await loadDepartments() : departments;
      const nextDeptId =
        canSelectDepartment && query.deptId
          ? nextDepartments.some((item) => item.id === query.deptId)
            ? query.deptId
            : undefined
          : query.deptId;

      if (nextDeptId !== query.deptId) {
        setQuery((previous) => ({
          ...previous,
          deptId: nextDeptId,
          pageNum: 1,
        }));
        return;
      }

      await loadLeaveManageList(
        {
          ...query,
          deptId: nextDeptId,
        },
        { refreshCache: false },
      );
    })();
  });

  const columns: ColumnsType<AttendanceLeaveManageItem> = [
    {
      title: '员工',
      dataIndex: 'employeeName',
      width: 140,
      render: (value, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{value || '--'}</Text>
          <Text type="secondary">{record.employeeId ? `ID ${record.employeeId}` : '--'}</Text>
        </Space>
      ),
    },
    {
      title: '工号',
      dataIndex: 'employeeNo',
      width: 120,
      render: (value) => value || '--',
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      width: 140,
      render: (value) => value || '--',
    },
    {
      title: '请假类型',
      dataIndex: 'leaveTypeDesc',
      width: 120,
      render: (value, record) => value || record.leaveType || '--',
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      width: 170,
      render: (value) => formatDateTime(value),
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 170,
      render: (value) => formatDateTime(value),
    },
    {
      title: '请假天数',
      dataIndex: 'totalDays',
      width: 100,
      render: (value) => (value == null ? '--' : `${value}`),
    },
    {
      title: '请假事由',
      dataIndex: 'leaveReason',
      ellipsis: true,
      render: (value) => value || '--',
    },
    {
      title: '审批状态',
      dataIndex: 'approvalStatus',
      width: 110,
      render: (value, record) => renderApprovalStatusTag(value, record.approvalStatusDesc),
    },
    {
      title: '当前审批节点',
      dataIndex: 'currentNodeName',
      width: 140,
      render: (value) => value || '--',
    },
    {
      title: '当前审批人',
      dataIndex: 'currentApproverName',
      width: 140,
      render: (value) => value || '--',
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      width: 170,
      render: (value) => formatDateTime(value),
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_value, record) =>
        record.approvalInstanceId ? (
          <Space size={4}>
            {record.approvalStatus === 1 ? (
              <Popconfirm
                title="快速审批通过请假申请"
                description="确认后将直接完成当前请假审批流程。"
                onConfirm={async () => {
                  await quickApproveAttendanceLeave(record.id);
                  message.success('已快速审批通过请假申请');
                  refreshCacheOnNextLoadRef.current = true;
                  setQuery((previous) => ({
                    ...previous,
                    pageNum: 1,
                  }));
                }}
              >
                <Button size="small" type="primary">
                  快速审批
                </Button>
              </Popconfirm>
            ) : null}
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => history.push(`/approval/detail/${record.approvalInstanceId}`)}
            >
              {record.approvalStatus === 1 ? '去处理' : '查看审批'}
            </Button>
          </Space>
        ) : (
          <Text type="secondary">--</Text>
        ),
    },
  ];

  /**
   * 查询并重置到第一页。
   */
  const handleSearch = (values: LeaveManageFilterValues) => {
    refreshCacheOnNextLoadRef.current = true;
    setQuery((previous) => ({
      ...previous,
      yearMonth: values.yearMonth?.format('YYYY-MM') || dayjs().format('YYYY-MM'),
      deptId: canSelectDepartment ? values.deptId : currentUser?.deptId,
      keyword: values.keyword?.trim() || undefined,
      approvalStatus: values.approvalStatus,
      pageNum: 1,
    }));
  };

  /**
   * 重置查询条件。
   */
  const handleReset = () => {
    setQuery({
      yearMonth: dayjs().format('YYYY-MM'),
      deptId: canSelectDepartment ? undefined : currentUser?.deptId,
      keyword: undefined,
      approvalStatus: undefined,
      pageNum: 1,
      pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
    });
  };

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            请假管理
          </Title>
          <Text type="secondary">
            面向管理者、HR 和部门主管的请假管理台账，支持按月份、部门、员工和审批状态筛选。
          </Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          initialValues={{
            yearMonth: dayjs(query.yearMonth, 'YYYY-MM'),
            deptId: query.deptId,
            keyword: query.keyword,
            approvalStatus: query.approvalStatus,
          }}
          style={{ rowGap: 16 }}
        >
          <Form.Item label="月份" name="yearMonth">
            <DatePicker picker="month" allowClear={false} />
          </Form.Item>
          {canSelectDepartment ? (
            <Form.Item label="部门" name="deptId">
              <Select
                showSearch
                allowClear
                loading={departmentLoading}
                placeholder="请选择部门"
                optionFilterProp="label"
                options={departmentOptions}
                style={{ width: 220 }}
              />
            </Form.Item>
          ) : null}
          <Form.Item label="员工" name="keyword">
            <Input allowClear placeholder="姓名/工号" style={{ width: 220 }} />
          </Form.Item>
          <Form.Item label="审批状态" name="approvalStatus">
            <Select
              allowClear
              placeholder="全部状态"
              options={DEFAULT_APPROVAL_STATUS_OPTIONS}
              style={{ width: 180 }}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        bordered={false}
        style={{ borderRadius: 20 }}
        title={`请假记录 · ${query.yearMonth}`}
        extra={<Text type="secondary">审批处理请跳转到审批详情页完成</Text>}
      >
        <Table<AttendanceLeaveManageItem>
          rowKey="id"
          columns={columns}
          dataSource={leavePageData?.records || []}
          loading={leaveLoading}
          scroll={{ x: 1740 }}
          locale={{ emptyText: '暂无请假管理数据' }}
          pagination={{
            current: leavePageData?.pageNum || query.pageNum,
            pageSize: leavePageData?.pageSize || query.pageSize,
            total: leavePageData?.total || 0,
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

export default AttendanceLeaveManagePage;

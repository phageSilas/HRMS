/**
 * 审批工作台页面
 *
 * 功能：待审批列表（含角标）、已审批列表、我发起的申请列表
 * 每个标签页支持搜索筛选和分页，点击行跳转审批详情
 */
import React, { useState, useEffect, useCallback } from 'react';
import { history } from '@umijs/max';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Tabs, Tag, Badge, DatePicker, Space, Select, Button, Form, Input, message } from 'antd';
import type { ApprovalTask, PendingQuery, MyApplicationQuery } from '@/services/approval';
import {
  getPendingTasks,
  getPendingCount,
  getHistoryTasks,
  getMyApplications,
} from '@/services/approval';

const { RangePicker } = DatePicker;

// ============ 常量定义 ============

/** 业务类型筛选选项（与后端 ApprovalTypeEnum 对齐） */
const BUSINESS_TYPE_OPTIONS = [
  { label: '全部', value: '' },
  { label: '入职申请', value: 'ENTRY' },
  { label: '转正申请', value: 'REGULAR' },
  { label: '调岗申请', value: 'TRANSFER' },
  { label: '离职审批', value: 'LEAVE' },
  { label: '请假审批', value: 'LEAVE_REQUEST' },
  { label: '补卡审批', value: 'CORRECTION' },
  { label: '薪资批次审批', value: 'SALARY' },
];

/** 审批状态筛选选项（用于「我发起的」标签页） */
const STATUS_OPTIONS = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
];

/** 状态 Tag 颜色映射（与后端 ApprovalStatusEnum 对齐） */
const STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  DRAFT: 'default',
  WITHDRAWN: 'warning',
  CANCELLED: 'default',
};

/** 状态码 → 中文名映射（用于后端未返回 statusName 时的降级） */
const STATUS_LABEL_MAP: Record<string, string> = {
  PENDING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  DRAFT: '草稿',
  WITHDRAWN: '已撤回',
  CANCELLED: '已取消',
};

// ============ 工具函数 ============

/**
 * 根据截止时间距现在的时长返回颜色
 * @param deadline - 截止时间字符串
 * @returns 颜色值或 undefined（无额外样式）
 *
 * 规则：
 *  - < 6 小时：红色 #f5222d（紧急）
 *  - < 24 小时：黄色 #faad14（即将到期）
 *  - 其他：无色
 */
const getDeadlineColor = (deadline?: string | null): string | undefined => {
  if (!deadline) return undefined; // 无截止时间时不标色（如我发起的 Tab）
  const now = Date.now();
  const deadlineTime = new Date(deadline).getTime();
  const diffHours = (deadlineTime - now) / (1000 * 60 * 60);
  if (diffHours < 6) return '#f5222d';
  if (diffHours < 24) return '#faad14';
  return undefined;
};

// ============ 表格列定义（三个标签页复用） ============

const TABLE_COLUMNS: ProColumns<ApprovalTask>[] = [
  { title: '申请标题', dataIndex: 'title', ellipsis: true },
  { title: '申请人', dataIndex: 'applicantName', width: 120 },
  { title: '业务类型', dataIndex: 'businessTypeName', width: 100 },
  {
    title: '申请时间',
    dataIndex: 'createdAt',
    width: 170,
    valueType: 'dateTime',
  },
  {
    title: '截止时间',
    dataIndex: 'deadline',
    width: 170,
    valueType: 'dateTime',
    render: (_, record) => (
      <span style={{ color: getDeadlineColor(record.deadline) }}>
        {record.deadline}
      </span>
    ),
  },
  {
    title: '当前节点',
    dataIndex: 'nodeName',
    width: 120,
  },
  {
    title: '状态',
    dataIndex: 'statusName',
    width: 100,
    render: (_, record) => (
      <Tag color={STATUS_COLOR_MAP[record.status] || 'default'}>
        {record.statusName || STATUS_LABEL_MAP[record.status] || record.status}
      </Tag>
    ),
  },
];

// ============ 页面组件 ============

const WorkspacePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('pending');
  const [badgeCount, setBadgeCount] = useState<number>(0);

  // 各标签页搜索参数（传递给 ProTable params 属性，变化时触发重新查询）
  const [pendingSearch, setPendingSearch] = useState<Partial<PendingQuery>>({});
  const [historySearch, setHistorySearch] = useState<Partial<PendingQuery>>({});
  const [mySearch, setMySearch] = useState<Partial<MyApplicationQuery>>({});

  // 搜索表单实例（保留表单状态，切换标签页时不清空）
  const [pendingFormInstance] = Form.useForm();
  const [historyFormInstance] = Form.useForm();

  // ============ 生命周期 ============

  /** 获取待审批角标数量 */
  const fetchBadgeCount = useCallback(async () => {
    try {
      const result = await getPendingCount();
      setBadgeCount(result.count);
    } catch {
      // pending-count 接口暂未就绪时静默降级
      setBadgeCount(0);
    }
  }, []);

  useEffect(() => {
    fetchBadgeCount();
  }, [fetchBadgeCount]);

  // ============ 待审批搜索/重置 ============

  /** 将 DatePicker 的 dateRange 值转为后端需要的 startDate/endDate */
  const formatDateRange = (dateRange: any[]) => {
    if (!dateRange || dateRange.length !== 2) return {};
    return {
      startDate: dateRange[0].format('YYYY-MM-DD HH:mm:ss'),
      endDate: dateRange[1].format('YYYY-MM-DD HH:mm:ss'),
    };
  };

  const handlePendingSearch = useCallback(async () => {
    try {
      const values = await pendingFormInstance.validateFields();
      const params: Partial<PendingQuery> = {};
      if (values.businessType) params.businessType = values.businessType;
      if (values.keyword) params.keyword = values.keyword;
      if (values.dateRange) {
        Object.assign(params, formatDateRange(values.dateRange));
      }
      setPendingSearch(params);
    } catch {
      // 表单校验不通过时不处理
    }
  }, [pendingFormInstance]);

  const handlePendingReset = useCallback(() => {
    pendingFormInstance.resetFields();
    setPendingSearch({});
  }, [pendingFormInstance]);

  // ============ 已审批搜索/重置 ============

  const handleHistorySearch = useCallback(async () => {
    try {
      const values = await historyFormInstance.validateFields();
      const params: Partial<PendingQuery> = {};
      if (values.businessType) params.businessType = values.businessType;
      if (values.keyword) params.keyword = values.keyword;
      if (values.dateRange) {
        Object.assign(params, formatDateRange(values.dateRange));
      }
      setHistorySearch(params);
    } catch {
      // 表单校验不通过时不处理
    }
  }, [historyFormInstance]);

  const handleHistoryReset = useCallback(() => {
    historyFormInstance.resetFields();
    setHistorySearch({});
  }, [historyFormInstance]);

  // ============ 我发起的筛选/重置 ============

  const handleMyStatusChange = useCallback(
    (value: string | undefined) => {
      setMySearch(value ? { status: value } : {});
    },
    [],
  );

  const handleMyReset = useCallback(() => {
    setMySearch({});
  }, []);

  // ============ ProTable request 回调 ============

  const pendingRequest = useCallback(async (params: Record<string, any>) => {
    const { current, pageSize, ...filters } = params;
    try {
      const result = await getPendingTasks({
        pageNum: current,
        pageSize,
        ...filters,
      } as PendingQuery);
      return { data: result.records, success: true, total: result.total };
    } catch {
      message.error('获取待审批列表失败');
      return { data: [], success: false, total: 0 };
    }
  }, []);

  const historyRequest = useCallback(async (params: Record<string, any>) => {
    const { current, pageSize, ...filters } = params;
    try {
      const result = await getHistoryTasks({
        pageNum: current,
        pageSize,
        ...filters,
      } as PendingQuery);
      return { data: result.records, success: true, total: result.total };
    } catch {
      message.error('获取已审批列表失败');
      return { data: [], success: false, total: 0 };
    }
  }, []);

  const myRequest = useCallback(async (params: Record<string, any>) => {
    const { current, pageSize, ...filters } = params;
    try {
      const result = await getMyApplications({
        pageNum: current,
        pageSize,
        ...filters,
      } as MyApplicationQuery);
      return { data: result.records, success: true, total: result.total };
    } catch {
      message.error('获取我的申请列表失败');
      return { data: [], success: false, total: 0 };
    }
  }, []);

  // ============ ProTable 公共配置 ============

  const commonProTableProps = {
    columns: TABLE_COLUMNS,
    rowKey: 'id' as const,
    search: false as const,
    pagination: {
      showSizeChanger: true,
      showTotal: (total: number) => `共 ${total} 条`,
    },
    onRow: (record: ApprovalTask) => ({
      onClick: () => history.push(`/approval/detail/${record.id}`),
      style: { cursor: 'pointer' } as React.CSSProperties,
    }),
  };

  // ============ 渲染 ============

  return (
    <PageContainer>
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        // destroyInactiveTabPane 默认为 false，切换 Tab 时保留筛选条件和分页状态
        items={[
          {
            key: 'pending',
            label: (
              <span>
                待审批
                {badgeCount > 0 && (
                  <Badge
                    count={badgeCount}
                    size="small"
                    style={{ marginInlineStart: 4 }}
                  />
                )}
              </span>
            ),
            children: (
              <>
                {/* 待审批搜索栏 */}
                <Form
                  form={pendingFormInstance}
                  layout="inline"
                  style={{ marginBottom: 16 }}
                >
                  <Form.Item name="businessType" label="业务类型">
                    <Select
                      options={BUSINESS_TYPE_OPTIONS}
                      style={{ width: 140 }}
                      placeholder="选择业务类型"
                      allowClear
                    />
                  </Form.Item>
                  <Form.Item name="keyword" label="关键词">
                    <Input
                      placeholder="申请标题/申请人"
                      style={{ width: 240 }}
                      allowClear
                    />
                  </Form.Item>
                  <Form.Item name="dateRange" label="申请时间">
                    <RangePicker />
                  </Form.Item>
                  <Form.Item>
                    <Space>
                      <Button type="primary" onClick={handlePendingSearch}>
                        查询
                      </Button>
                      <Button onClick={handlePendingReset}>重置</Button>
                    </Space>
                  </Form.Item>
                </Form>
                {/* 待审批列表 */}
                <ProTable<ApprovalTask>
                  {...commonProTableProps}
                  request={pendingRequest}
                  params={pendingSearch}
                />
              </>
            ),
          },
          {
            key: 'history',
            label: '已审批',
            children: (
              <>
                {/* 已审批搜索栏 */}
                <Form
                  form={historyFormInstance}
                  layout="inline"
                  style={{ marginBottom: 16 }}
                >
                  <Form.Item name="businessType" label="业务类型">
                    <Select
                      options={BUSINESS_TYPE_OPTIONS}
                      style={{ width: 140 }}
                      placeholder="选择业务类型"
                      allowClear
                    />
                  </Form.Item>
                  <Form.Item name="keyword" label="关键词">
                    <Input
                      placeholder="申请标题/申请人"
                      style={{ width: 240 }}
                      allowClear
                    />
                  </Form.Item>
                  <Form.Item name="dateRange" label="申请时间">
                    <RangePicker />
                  </Form.Item>
                  <Form.Item>
                    <Space>
                      <Button type="primary" onClick={handleHistorySearch}>
                        查询
                      </Button>
                      <Button onClick={handleHistoryReset}>重置</Button>
                    </Space>
                  </Form.Item>
                </Form>
                {/* 已审批列表 */}
                <ProTable<ApprovalTask>
                  {...commonProTableProps}
                  request={historyRequest}
                  params={historySearch}
                />
              </>
            ),
          },
          {
            key: 'my',
            label: '我发起的',
            children: (
              <>
                {/* 我发起的筛选栏（仅状态筛选） */}
                <Space style={{ marginBottom: 16 }}>
                  <span>状态：</span>
                  <Select
                    allowClear
                    placeholder="选择审批状态"
                    style={{ width: 140 }}
                    options={STATUS_OPTIONS}
                    value={mySearch.status}
                    onChange={handleMyStatusChange}
                  />
                  <Button onClick={handleMyReset}>重置</Button>
                </Space>
                {/* 我发起的列表 */}
                <ProTable<ApprovalTask>
                  {...commonProTableProps}
                  request={myRequest}
                  params={mySearch}
                />
              </>
            ),
          },
        ]}
      />
    </PageContainer>
  );
};

export default WorkspacePage;

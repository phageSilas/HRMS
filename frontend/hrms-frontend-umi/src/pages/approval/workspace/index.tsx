/**
 * 审批工作台页面（卡片式布局）
 *
 * 核心功能：
 * - 顶部三个统计卡片（待审批 / 今日已审批 / 已逾期），点击可切换列表筛选
 * - 关键词搜索 + 业务类型下拉筛选
 * - 卡片式任务列表，每条展示申请人信息、时间节点、操作按钮
 * - 弹出式审批操作（通过 / 拒绝），支持填写审批意见
 * - 加载态 / 错误态 / 空态处理
 *
 * 数据流：
 * 初始化时加载统计数据 + 列表数据，
 * 搜索/分页/筛选变化时重新请求 getTasks，
 * 操作成功后自动刷新统计和列表。
 *
 * @module ApprovalWorkspace
 */

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Card,
  Tag,
  Space,
  Select,
  Button,
  Input,
  message,
  Avatar,
  Row,
  Col,
  Pagination,
  Spin,
  Empty,
  Modal,
  Form,
} from 'antd';
import {
  SearchOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  UserOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ApprovalTask, PendingQuery } from '@/services/approval';
import {
  getTasks,
  getPendingCount,
  getTodayApprovedCount,
  getOverdueCount,
  operateApproval,
} from '@/services/approval';

// ============ 业务类型标签颜色映射 ============

/** 业务类型编码 → Tag 颜色映射 */
const BUSINESS_TYPE_COLOR_MAP: Record<string, string> = {
  ENTRY: 'green',        // 入职审批
  REGULAR: 'blue',       // 转正审批
  TRANSFER: 'purple',    // 调岗审批
  LEAVE: 'red',          // 离职审批
  LEAVE_REQUEST: 'orange', // 请假审批
  CORRECTION: 'cyan',    // 补卡审批
  OVERTIME: 'geekblue',  // 加班审批
  SALARY: 'magenta',     // 薪资审批
};

/** 业务类型筛选选项 */
const BUSINESS_TYPE_OPTIONS = [
  { label: '全部类型', value: '' },
  { label: '入职申请', value: 'ENTRY' },
  { label: '转正申请', value: 'REGULAR' },
  { label: '调岗申请', value: 'TRANSFER' },
  { label: '离职审批', value: 'LEAVE' },
  { label: '请假审批', value: 'LEAVE_REQUEST' },
  { label: '补卡审批', value: 'CORRECTION' },
  { label: '加班审批', value: 'OVERTIME' },
  { label: '薪资批次审批', value: 'SALARY' },
];

// ============ 页面组件 ============

/** 审批工作台主页面 */
const WorkspacePage: React.FC = () => {
  // ---- 统计卡片数据 ----
  const [pendingCount, setPendingCount] = useState<number>(0);
  const [todayApprovedCount, setTodayApprovedCount] = useState<number>(0);
  const [overdueCount, setOverdueCount] = useState<number>(0);
  const [statsLoading, setStatsLoading] = useState<boolean>(false);

  // ---- 列表数据 ----
  const [taskList, setTaskList] = useState<ApprovalTask[]>([]);
  const [listLoading, setListLoading] = useState<boolean>(false);
  const [listError, setListError] = useState<string | null>(null);
  const [total, setTotal] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(10);

  // ---- 搜索筛选 ----
  const [keyword, setKeyword] = useState<string>('');
  const [businessType, setBusinessType] = useState<string>('');
  /** 筛选类型（统计卡片点击控制）：pending / today-approved / overdue */
  const [filterType, setFilterType] = useState<string>('pending');
  /**
   * 搜索版本号 — 递增时触发重新加载
   *
   * 用于解决"同参数重复搜索"时 useEffect 无法检测变更的问题。
   * 每次点击查询/重置/卡片筛选时递增该值。
   */
  const [searchVersion, setSearchVersion] = useState<number>(0);

  // ---- 操作弹窗 ----
  const [operateModal, setOperateModal] = useState<{
    visible: boolean;
    action: 'approve' | 'reject';
    taskId?: number;
    taskTitle?: string;
  }>({ visible: false, action: 'approve' });
  const [operateLoading, setOperateLoading] = useState<boolean>(false);
  const [operateForm] = Form.useForm();

  // ============ 数据加载 ============

  /**
   * 加载统计卡片数据
   *
   * 并行请求待审批数、今日已审批数、已逾期数三个统计数据。
   * 接口异常时静默降级为默认值 0。
   */
  const fetchStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const [pendingRes, todayRes, overdueRes] = await Promise.all([
        getPendingCount(),
        getTodayApprovedCount(),
        getOverdueCount(),
      ]);
      setPendingCount(pendingRes.count ?? 0);
      setTodayApprovedCount(todayRes.count ?? 0);
      setOverdueCount(overdueRes.count ?? 0);
    } catch {
      // 接口异常时使用默认值 0
    } finally {
      setStatsLoading(false);
    }
  }, []);

  /**
   * 加载任务列表
   *
   * 所有参数显式传入，不依赖闭包，确保在 useEffect 中调用时行为一致。
   *
   * @param params.page  页码
   * @param params.size  每页条数
   * @param params.kw    关键词
   * @param params.biz   业务类型筛选
   * @param params.filter 筛选类型（pending/today-approved/overdue）
   */
  const fetchTaskList = useCallback(
    async (params: { page: number; size: number; kw: string; biz: string; filter: string }) => {
      setListLoading(true);
      setListError(null);
      try {
        const query: PendingQuery = {
          pageNum: params.page,
          pageSize: params.size,
          filterType: params.filter,
        };
        if (params.kw.trim()) query.keyword = params.kw.trim();
        if (params.biz) query.businessType = params.biz;
        const result = await getTasks(query);
        setTaskList(result.records ?? []);
        setTotal(result.total ?? 0);
      } catch (err: any) {
        setListError(err?.message || '加载失败');
        setTaskList([]);
      } finally {
        setListLoading(false);
      }
    },
    [],
  );

  // 初始化时加载统计数据
  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  // 列表加载：分页变化、主动搜索或筛选卡片切换时触发
  useEffect(() => {
    fetchTaskList({ page: currentPage, size: pageSize, kw: keyword, biz: businessType, filter: filterType });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, pageSize, searchVersion, filterType]);

  // ============ 搜索操作 ============

  /** 触发搜索：重置到第一页 + 递增版本号触发重新加载 */
  const handleSearch = useCallback(() => {
    setCurrentPage(1);
    setSearchVersion((v) => v + 1);
  }, []);

  /** 重置所有搜索条件和筛选 */
  const handleReset = useCallback(() => {
    setKeyword('');
    setBusinessType('');
    setFilterType('pending');
    setCurrentPage(1);
    setSearchVersion((v) => v + 1);
  }, []);

  // ============ 审批操作 ============

  /** 打开审批操作弹窗 */
  const showOperateModal = (action: 'approve' | 'reject', task: ApprovalTask) => {
    operateForm.resetFields();
    setOperateModal({
      visible: true,
      action,
      taskId: task.taskId ?? task.id,
      taskTitle: task.title,
    });
  };

  /** 关闭审批操作弹窗 */
  const closeOperateModal = () => {
    setOperateModal({ visible: false, action: 'approve' });
    operateForm.resetFields();
  };

  /**
   * 执行审批操作（通过 / 拒绝）
   *
   * 提交成功后自动刷新统计卡片和任务列表，
   * 表单校验失败时（Ant Design 错误）不处理。
   */
  const handleOperate = useCallback(async () => {
    const { taskId, action } = operateModal;
    if (!taskId) return;
    try {
      const values = await operateForm.validateFields();
      setOperateLoading(true);
      await operateApproval(taskId, {
        action,
        comment: values.comment,
      });
      message.success(action === 'approve' ? '已通过' : '已拒绝');
      closeOperateModal();
      fetchStats();
      fetchTaskList({ page: currentPage, size: pageSize, kw: keyword, biz: businessType, filter: filterType });
    } catch (error: any) {
      if (error?.errorFields) return;
      message.error(error?.message || '操作失败');
    } finally {
      setOperateLoading(false);
    }
  }, [operateModal, operateForm, fetchStats, fetchTaskList, currentPage, pageSize, keyword, businessType]);

  // ============ 页面变化 ============

  const handlePageChange = (page: number, size: number) => {
    setCurrentPage(page);
    setPageSize(size);
  };

  // ============ 动态空状态文案 ============

  const emptyMessage = useMemo(() => {
    switch (filterType) {
      case 'today-approved':
        return '今日暂无已审批事项';
      case 'overdue':
        return '暂无逾期事项';
      default:
        return '暂无待审批事项';
    }
  }, [filterType]);

  // ============ 统计卡片配置 ============

  const STAT_CARDS = [
    {
      key: 'pending',
      label: '待审批',
      count: pendingCount,
      icon: <ClockCircleOutlined />,
      bgGradient: 'linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%)',
      iconBg: '#faad14',
      color: '#faad14',
      activeBorder: '#faad14',
    },
    {
      key: 'today-approved',
      label: '今日已审批',
      count: todayApprovedCount,
      icon: <CheckCircleOutlined />,
      bgGradient: 'linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%)',
      iconBg: '#52c41a',
      color: '#52c41a',
      activeBorder: '#52c41a',
    },
    {
      key: 'overdue',
      label: '已逾期',
      count: overdueCount,
      icon: <ExclamationCircleOutlined />,
      bgGradient: 'linear-gradient(135deg, #fff2f0 0%, #ffd8d2 100%)',
      iconBg: '#ff4d4f',
      color: '#ff4d4f',
      activeBorder: '#ff4d4f',
    },
  ] as const;

  // ============ 渲染统计卡片 ============

  const renderStatCards = () => (
    <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
      {STAT_CARDS.map((card) => {
        const isActive = filterType === card.key;
        return (
          <Col span={8} key={card.key}>
            <Card
              hoverable
              style={{
                background: card.bgGradient,
                borderRadius: 12,
                border: isActive ? `2px solid ${card.activeBorder}` : 'none',
                boxShadow: isActive
                  ? `0 4px 12px ${card.activeBorder}33`
                  : '0 2px 8px rgba(0,0,0,0.06)',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
              }}
              bodyStyle={{ padding: '20px 24px' }}
              onClick={() => {
                // 再次点击已选中卡片 → 回到"待审批"视图
                if (filterType === card.key) {
                  setFilterType('pending');
                } else {
                  setFilterType(card.key);
                }
                setCurrentPage(1);
                setSearchVersion((v) => v + 1);
              }}
            >
              <Spin spinning={statsLoading} size="small">
                <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                  <div
                    style={{
                      width: 48,
                      height: 48,
                      borderRadius: 12,
                      background: card.iconBg,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {React.cloneElement(card.icon as React.ReactElement, {
                      style: { fontSize: 24, color: '#fff' },
                    })}
                  </div>
                  <div>
                    <div
                      style={{
                        fontSize: 32,
                        fontWeight: 700,
                        color: card.color,
                        lineHeight: 1.2,
                      }}
                    >
                      {card.count}
                    </div>
                    <div style={{ fontSize: 14, color: '#8c8c8c' }}>{card.label}</div>
                  </div>
                </div>
              </Spin>
            </Card>
          </Col>
        );
      })}
    </Row>
  );

  // ============ 渲染搜索筛选区 ============

  const renderSearchBar = () => (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 20,
        flexWrap: 'wrap',
        gap: 12,
      }}
    >
      <Input
        placeholder="搜索申请人或单号..."
        prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        onPressEnter={handleSearch}
        style={{ width: 320, borderRadius: 8 }}
        allowClear
      />
      <Space size={12}>
        <Select
          value={businessType}
          onChange={setBusinessType}
          style={{ width: 140, borderRadius: 8 }}
          options={BUSINESS_TYPE_OPTIONS}
        />
        <Button type="primary" onClick={handleSearch} style={{ borderRadius: 8 }}>
          查询
        </Button>
        <Button onClick={handleReset} style={{ borderRadius: 8 }}>
          重置
        </Button>
      </Space>
    </div>
  );

  // ============ 渲染业务类型标签 ============

  const renderBusinessTypeTag = (type: string, name: string) => {
    const color = BUSINESS_TYPE_COLOR_MAP[type] || 'default';
    return <Tag color={color} style={{ borderRadius: 4 }}>{name || type}</Tag>;
  };

  // ============ 渲染任务卡片 ============

  const renderTaskCard = (task: ApprovalTask) => {
    const isOverdue = task.overdue ?? false;

    return (
      <Card
        key={task.id}
        style={{
          marginBottom: 16,
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          border: '1px solid #f0f0f0',
        }}
        bodyStyle={{ padding: '20px 24px' }}
        hoverable
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 16 }}>
          {/* 左侧：头像 + 姓名 + 标签 */}
          <div style={{ minWidth: 180, flexShrink: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <Avatar
                size={40}
                icon={<UserOutlined />}
                src={task.applicantAvatar}
                style={{
                  backgroundColor: task.applicantAvatar ? undefined : '#1890ff',
                  flexShrink: 0,
                }}
              >
                {task.applicantName?.[0]}
              </Avatar>
              <div>
                <div style={{ fontSize: 15, fontWeight: 600, color: '#262626' }}>
                  {task.applicantName}
                </div>
                {task.applicantDeptName && (
                  <Tag
                    color="green"
                    style={{ borderRadius: 4, fontSize: 12, marginTop: 2 }}
                  >
                    {task.applicantDeptName}
                  </Tag>
                )}
              </div>
            </div>
            <div>{renderBusinessTypeTag(task.businessType, task.businessTypeName)}</div>
          </div>

          {/* 中间：时间信息 + 当前审批节点 */}
          <div style={{ flex: 1, minWidth: 200 }}>
            <div style={{ fontSize: 13, color: '#8c8c8c', lineHeight: 2 }}>
              <div>
                申请：{task.createdAt}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span>截止：{task.deadline || '-'}</span>
                {isOverdue && (
                  <Tag color="red" style={{ borderRadius: 4, fontSize: 11 }}>
                    已逾期
                  </Tag>
                )}
              </div>
              <div style={{ color: '#bfbfbf' }}>
                当前节点：{task.nodeName}
              </div>
            </div>
          </div>

          {/* 右侧：操作按钮 */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, justifyContent: 'center', minWidth: 100 }}>
            <Button
              type="default"
              style={{ borderRadius: 8, borderColor: '#d9d9d9' }}
              onClick={() => history.push(`/approval/detail/${task.id}`)}
            >
              查看详情
            </Button>
            {isOverdue ? (
              <Button
                disabled
                style={{
                  borderRadius: 8,
                  color: '#ff4d4f',
                  borderColor: '#ff4d4f',
                  cursor: 'not-allowed',
                }}
              >
                已逾期，无法操作
              </Button>
            ) : (
              <>
                <Button
                  type="primary"
                  style={{
                    borderRadius: 8,
                    background: '#52c41a',
                    borderColor: '#52c41a',
                  }}
                  onClick={() => showOperateModal('approve', task)}
                >
                  通过
                </Button>
                <Button
                  danger
                  style={{ borderRadius: 8 }}
                  onClick={() => showOperateModal('reject', task)}
                >
                  拒绝
                </Button>
              </>
            )}
          </div>
        </div>
      </Card>
    );
  };

  // ============ 渲染列表区域 ============

  const renderTaskList = () => {
    // 首次加载态
    if (listLoading && taskList.length === 0) {
      return (
        <div style={{ textAlign: 'center', padding: '80px 0' }}>
          <Spin size="large" />
        </div>
      );
    }

    // 错误态：展示错误信息和重新加载按钮
    if (listError) {
      return (
        <Card style={{ borderRadius: 12, textAlign: 'center', padding: '40px 0' }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>😵</div>
          <div style={{ fontSize: 16, color: '#999', marginBottom: 16 }}>{listError}</div>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => fetchTaskList({ page: currentPage, size: pageSize, kw: keyword, biz: businessType, filter: filterType })}
            style={{ borderRadius: 8 }}
          >
            重新加载
          </Button>
        </Card>
      );
    }

    // 空态：根据当前筛选类型展示不同的空状态提示
    if (taskList.length === 0) {
      return (
        <Card style={{ borderRadius: 12 }}>
          <Empty
            description={emptyMessage}
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        </Card>
      );
    }

    // 正常列表
    return (
      <>
        {taskList.map(renderTaskCard)}
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={total}
            onChange={handlePageChange}
            showSizeChanger
            showTotal={(t) => `共 ${t} 条`}
          />
        </div>
      </>
    );
  };

  // ============ 渲染 ============

  return (
    <PageContainer>
      <div style={{ marginBottom: 24 }}>
        <h2 style={{ margin: 0, fontSize: 22, fontWeight: 600, color: '#262626' }}>
          审批工作台
        </h2>
        <p style={{ margin: '4px 0 0', color: '#8c8c8c', fontSize: 14 }}>
          管理和处理所有待审批事项
        </p>
      </div>

      {renderStatCards()}
      {renderSearchBar()}
      {renderTaskList()}

      {/* 审批操作弹窗 */}
      <Modal
        title={operateModal.action === 'approve' ? '审批通过' : '审批拒绝'}
        open={operateModal.visible}
        onOk={handleOperate}
        onCancel={closeOperateModal}
        confirmLoading={operateLoading}
        destroyOnClose
      >
        <Form form={operateForm} layout="vertical">
          <Form.Item
            name="comment"
            label="审批意见"
            rules={
              operateModal.action === 'reject'
                ? [{ required: true, message: '拒绝时必须填写意见' }]
                : []
            }
          >
            <Input.TextArea
              rows={4}
              placeholder={
                operateModal.action === 'reject'
                  ? '请填写拒绝原因'
                  : '可选填写审批意见'
              }
            />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default WorkspacePage;

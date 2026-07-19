import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import { getDeptList, type DeptListItem } from '@/services/organization';
import type { UserInfo } from '@/types/user';
import {
  calculateSalaryBatch,
  createSalaryBatch,
  getCurrentSalaryBatch,
  getSalaryBatchTrend,
  previewSalaryBatch,
  recalculateSalaryBatch,
  saveSalaryBatchAdjustments,
  submitSalaryBatch,
  type SalaryBatch,
  type SalaryBatchAdjustmentItem,
  type SalaryBatchItem,
  type SalaryBatchPreview,
  type SalaryBatchTrendItem,
} from '@/services/salary';
import {
  CalculatorOutlined,
  CheckCircleOutlined,
  EditOutlined,
  EyeOutlined,
  FileDoneOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Column, Line, Pie } from '@ant-design/charts';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Descriptions,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Steps,
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

const STORAGE_PREFIX = 'salary-batch-month';
const MANAGEMENT_ROLES = new Set(['FINANCE', 'HR', 'HR_TEST', 'ADMIN', 'ROLE_ADMIN']);
const PREVIEW_DEFAULT_PAGE_SIZE = 10;
const ADJUSTMENT_ITEM_OPTIONS = [
  { label: '补贴', value: 'ALLOWANCE' },
  { label: '绩效奖金', value: 'PERFORMANCE_BONUS' },
  { label: '加班费', value: 'OVERTIME_PAY' },
  { label: '迟到扣款', value: 'LATE_DEDUCTION' },
  { label: '请假扣款', value: 'LEAVE_DEDUCTION' },
  { label: '社保', value: 'SOCIAL_INSURANCE' },
  { label: '公积金', value: 'HOUSING_FUND' },
  { label: '个税', value: 'INCOME_TAX' },
];

const BATCH_STATUS_STEPS = [
  { key: 'DRAFT', title: '草稿' },
  { key: 'CALCULATING', title: '计算中' },
  { key: 'PENDING_REVIEW', title: '待确认' },
  { key: 'APPROVING', title: '审批中' },
  { key: 'APPROVED', title: '已通过' },
  { key: 'RELEASED', title: '已发放' },
];

const COMPOSITION_COLORS = ['#1677ff', '#13c2c2', '#fa8c16', '#b37feb'];

interface AdjustmentFormValues {
  adjustments: SalaryBatchAdjustmentItem[];
}

function getCurrentUserFromStorage() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return undefined;
  }

  try {
    return JSON.parse(userInfoText) as UserInfo;
  } catch {
    return undefined;
  }
}

function resolveStorageKey() {
  const currentUser = getCurrentUserFromStorage();
  const identity =
    currentUser?.userId || currentUser?.username || currentUser?.nickname || 'anonymous';
  return `${STORAGE_PREFIX}:${identity}`;
}

function getStoredMonth() {
  const stored = sessionStorage.getItem(resolveStorageKey());
  if (!stored) {
    return dayjs().format('YYYY-MM');
  }
  return stored;
}

function isManagementRole(currentUser?: UserInfo) {
  return MANAGEMENT_ROLES.has(currentUser?.roleCode || '');
}

function toNumber(value?: number | string | null) {
  if (typeof value === 'number') {
    return value;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatCurrency(value?: number | string | null) {
  return `¥${toNumber(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

function getBatchStatusIndex(status?: string) {
  const current = BATCH_STATUS_STEPS.findIndex((item) => item.key === status);
  if (current >= 0) {
    return current;
  }
  if (status === 'ARCHIVED') {
    return BATCH_STATUS_STEPS.length - 1;
  }
  return 0;
}

function renderWarningTag(level?: string) {
  if (!level) {
    return <Tag color="success">正常</Tag>;
  }
  if (level === 'BLOCK') {
    return <Tag color="error">阻断异常</Tag>;
  }
  if (level === 'RED') {
    return <Tag color="volcano">高危预警</Tag>;
  }
  if (level === 'YELLOW') {
    return <Tag color="warning">待复核</Tag>;
  }
  if (level === 'NONE') {
    return <Tag color="success">无异常</Tag>;
  }
  return <Tag>{level}</Tag>;
}

function buildTrendChartData(items: SalaryBatchTrendItem[]) {
  return items.flatMap((item) => [
    {
      month: item.month,
      type: '应发总额',
      amount: toNumber(item.grossSalary),
      employeeCount: item.employeeCount || 0,
    },
    {
      month: item.month,
      type: '实发总额',
      amount: toNumber(item.netSalary),
      employeeCount: item.employeeCount || 0,
    },
  ]);
}

function buildDeptChartData(items: SalaryBatchItem[]) {
  const deptMap = new Map<
    string,
    { deptName: string; grossSalary: number; netSalary: number; employeeCount: number }
  >();

  items.forEach((item) => {
    const deptName = item.deptName || '未知部门';
    const current =
      deptMap.get(deptName) || {
        deptName,
        grossSalary: 0,
        netSalary: 0,
        employeeCount: 0,
      };
    current.grossSalary += toNumber(item.grossSalary);
    current.netSalary += toNumber(item.netSalary);
    current.employeeCount += 1;
    deptMap.set(deptName, current);
  });

  return Array.from(deptMap.values());
}

function buildCompositionData(items: SalaryBatchItem[]) {
  const totals = {
    基本工资: 0,
    补贴: 0,
    绩效奖金: 0,
    加班费: 0,
  };

  items.forEach((item) => {
    totals.基本工资 += toNumber(item.baseSalary);
    totals.补贴 += toNumber(item.allowance);
    totals.绩效奖金 += toNumber(item.performanceBonus);
    totals.加班费 += toNumber(item.overtimePay);
  });

  const totalAmount = Object.values(totals).reduce((sum, amount) => sum + amount, 0);

  return Object.entries(totals)
    .map(([type, amount], index) => {
      const percent = totalAmount > 0 ? amount / totalAmount : 0;
      return {
        type,
        amount,
        percent,
        percentText: `${(percent * 100).toFixed(1)}%`,
        labelText: `${type} ${`${(percent * 100).toFixed(1)}%`}`,
        color: COMPOSITION_COLORS[index % COMPOSITION_COLORS.length],
      };
    })
    .filter((item) => item.amount > 0);
}

function buildSocialFundData(items: SalaryBatchItem[]) {
  const totals = {
    社保: 0,
    公积金: 0,
  };

  items.forEach((item) => {
    totals.社保 += toNumber(item.socialInsurance);
    totals.公积金 += toNumber(item.housingFund);
  });

  return Object.entries(totals).map(([type, amount]) => ({ type, amount }));
}

const SalaryBatchPage: React.FC = () => {
  const currentUser = getCurrentUserFromStorage();
  const canManage = isManagementRole(currentUser);
  const [month, setMonth] = useState(getStoredMonth());
  const [currentBatch, setCurrentBatch] = useState<SalaryBatch | null>();
  const [previewData, setPreviewData] = useState<SalaryBatchPreview>();
  const [trendData, setTrendData] = useState<SalaryBatchTrendItem[]>([]);
  const [departmentOptions, setDepartmentOptions] = useState<DeptListItem[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [selectedDeptName, setSelectedDeptName] = useState<string>();
  const [onlyAbnormalPreview, setOnlyAbnormalPreview] = useState(false);
  const [previewPageNum, setPreviewPageNum] = useState(1);
  const [previewPageSize, setPreviewPageSize] = useState(PREVIEW_DEFAULT_PAGE_SIZE);
  const [loadingCurrent, setLoadingCurrent] = useState(false);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [loadingTrend, setLoadingTrend] = useState(false);
  const [creating, setCreating] = useState(false);
  const [recalculating, setRecalculating] = useState(false);
  const [submittingApproval, setSubmittingApproval] = useState(false);
  const [adjustmentModalOpen, setAdjustmentModalOpen] = useState(false);
  const [adjustmentSubmitting, setAdjustmentSubmitting] = useState(false);
  const [adjustingItem, setAdjustingItem] = useState<SalaryBatchItem>();
  const [adjustmentForm] = Form.useForm<AdjustmentFormValues>();
  const pollingRef = useRef<number>();

  const loadCurrentBatch = async (selectedMonth: string) => {
    setLoadingCurrent(true);
    try {
      const batch = await getCurrentSalaryBatch({
        salaryMonth: selectedMonth,
        scopeType: 'ALL',
      });
      setCurrentBatch(batch);
      return batch;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '当前薪资批次加载失败';
      message.error(messageText);
      setCurrentBatch(null);
      return null;
    } finally {
      setLoadingCurrent(false);
    }
  };

  const loadPreview = async (batchId: number) => {
    setLoadingPreview(true);
    try {
      const preview = await previewSalaryBatch(batchId);
      setPreviewData(preview);
      return preview;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '薪资预览加载失败';
      message.error(messageText);
      return undefined;
    } finally {
      setLoadingPreview(false);
    }
  };

  const loadTrend = async (selectedMonth: string) => {
    if (!canManage) {
      setTrendData([]);
      return;
    }
    setLoadingTrend(true);
    try {
      const trend = await getSalaryBatchTrend({
        anchorMonth: selectedMonth,
        months: 6,
        scopeType: 'ALL',
      });
      setTrendData(trend || []);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '月度薪资趋势加载失败';
      message.error(messageText);
    } finally {
      setLoadingTrend(false);
    }
  };

  const loadDepartments = async () => {
    setDepartmentLoading(true);
    try {
      const departments = await getDeptList();
      setDepartmentOptions(departments || []);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '部门列表加载失败';
      message.error(messageText);
    } finally {
      setDepartmentLoading(false);
    }
  };

  const loadWorkspace = async (selectedMonth: string) => {
    const batch = await loadCurrentBatch(selectedMonth);
    if (batch?.id) {
      await Promise.all([loadPreview(batch.id), loadTrend(selectedMonth)]);
      return;
    }
    setPreviewData(undefined);
    setTrendData([]);
  };

  useEffect(() => {
    sessionStorage.setItem(resolveStorageKey(), month);
  }, [month]);

  useEffect(() => {
    void loadWorkspace(month);
  }, [month]);

  useEffect(() => {
    void loadDepartments();
  }, []);

  useEffect(() => {
    setPreviewPageNum(1);
  }, [onlyAbnormalPreview, selectedDeptName, previewData?.batch?.id]);

  useEffect(() => {
    if (pollingRef.current) {
      window.clearTimeout(pollingRef.current);
    }
    if (currentBatch?.batchStatus !== 'CALCULATING') {
      return;
    }
    pollingRef.current = window.setTimeout(() => {
      void loadWorkspace(month);
    }, 4000);
    return () => {
      if (pollingRef.current) {
        window.clearTimeout(pollingRef.current);
      }
    };
  }, [currentBatch?.batchStatus, month]);

  usePageAutoRefresh(() => {
    void loadWorkspace(month);
  });

  const handleCreateBatch = async () => {
    setCreating(true);
    try {
      const batch = await createSalaryBatch({
        salaryMonth: month,
        scopeType: 'ALL',
      });
      await calculateSalaryBatch(batch.id);
      message.success('薪资核算批次已创建并开始计算');
      await loadWorkspace(month);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '新建核算批次失败';
      message.error(messageText);
    } finally {
      setCreating(false);
    }
  };

  const handleRecalculate = async () => {
    if (!currentBatch?.id) {
      return;
    }
    setRecalculating(true);
    try {
      await recalculateSalaryBatch(currentBatch.id);
      message.success('薪资批次已重新计算');
      await loadWorkspace(month);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '重新计算失败';
      message.error(messageText);
    } finally {
      setRecalculating(false);
    }
  };

  const handleSubmitApproval = async () => {
    if (!currentBatch?.id) {
      return;
    }
    setSubmittingApproval(true);
    try {
      const nextBatch = await submitSalaryBatch(currentBatch.id);
      setCurrentBatch(nextBatch);
      message.success('薪资批次已提交审批');
      await loadWorkspace(month);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '提交审批失败';
      message.error(messageText);
    } finally {
      setSubmittingApproval(false);
    }
  };

  const handleSubmitApprovalAction = () => {
    if (!currentBatch?.id) {
      return;
    }
    if (toNumber(currentBatch.blockCount) > 0) {
      Modal.warning({
        title: '无法提交审批',
        content: '当前批次存在阻断异常，请先处理完成后再提交审批。',
        okText: '我知道了',
      });
      return;
    }
    Modal.confirm({
      title: '是否提交审批',
      content: '提交后将进入审批流程，确认继续提交当前薪资批次吗？',
      okText: '是',
      cancelText: '否',
      onOk: async () => {
        await handleSubmitApproval();
      },
    });
  };

  const openAdjustmentModal = (item: SalaryBatchItem) => {
    setAdjustingItem(item);
    adjustmentForm.resetFields();
    adjustmentForm.setFieldsValue({
      adjustments: [
        {
          itemCode: 'ALLOWANCE',
          adjustAmount: 0,
          reason: '',
        },
      ],
    });
    setAdjustmentModalOpen(true);
  };

  const closeAdjustmentModal = () => {
    setAdjustmentModalOpen(false);
    setAdjustingItem(undefined);
    adjustmentForm.resetFields();
  };

  const handleSaveAdjustments = async () => {
    if (!currentBatch?.id || !adjustingItem?.employeeId) {
      return;
    }
    try {
      const values = await adjustmentForm.validateFields();
      setAdjustmentSubmitting(true);
      await saveSalaryBatchAdjustments(currentBatch.id, {
        employeeId: adjustingItem.employeeId,
        adjustments: values.adjustments,
      });
      message.success('人工调整已保存');
      closeAdjustmentModal();
      await loadWorkspace(month);
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setAdjustmentSubmitting(false);
    }
  };

  const summary = useMemo(() => {
    const batch = previewData?.batch || currentBatch;
    const items = previewData?.items || [];
    return {
      totalCount: batch?.totalCount ?? items.length,
      totalGrossSalary: batch?.totalGrossSalary ?? items.reduce((sum, item) => sum + toNumber(item.grossSalary), 0),
      totalNetSalary: batch?.totalNetSalary ?? items.reduce((sum, item) => sum + toNumber(item.netSalary), 0),
      warnings:
        toNumber(batch?.yellowWarningCount) +
        toNumber(batch?.redWarningCount) +
        toNumber(batch?.blockCount),
    };
  }, [currentBatch, previewData]);

  const trendChartData = useMemo(() => buildTrendChartData(trendData), [trendData]);
  const deptChartData = useMemo(
    () => buildDeptChartData(previewData?.items || []),
    [previewData?.items],
  );
  const compositionData = useMemo(
    () => buildCompositionData(previewData?.items || []),
    [previewData?.items],
  );
  const socialFundData = useMemo(
    () => buildSocialFundData(previewData?.items || []),
    [previewData?.items],
  );

  const canRecalculate =
    canManage && currentBatch?.batchStatus === 'PENDING_REVIEW';
  const canSubmitApproval =
    canManage && currentBatch?.batchStatus === 'PENDING_REVIEW';

  const filteredPreviewItems = useMemo(() => {
    const items = (previewData?.items || []).map((item) =>
      item.warningLevel === 'NONE'
        ? {
            ...item,
            warningReason: undefined,
          }
        : item,
    );
    const abnormalFilteredItems = onlyAbnormalPreview
      ? items.filter((item) => item.warningLevel && item.warningLevel !== 'NONE')
      : items;
    if (!selectedDeptName) {
      return abnormalFilteredItems;
    }
    return abnormalFilteredItems.filter((item) => item.deptName === selectedDeptName);
  }, [onlyAbnormalPreview, previewData?.items, selectedDeptName]);

  const pagedPreviewItems = useMemo(() => {
    const startIndex = (previewPageNum - 1) * previewPageSize;
    return filteredPreviewItems.slice(startIndex, startIndex + previewPageSize);
  }, [filteredPreviewItems, previewPageNum, previewPageSize]);

  const columns: ColumnsType<SalaryBatchItem> = [
    {
      title: '工号',
      dataIndex: 'employeeNo',
      width: 110,
      render: (value) => value || '--',
    },
    {
      title: '姓名',
      dataIndex: 'employeeName',
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
      title: '基本工资',
      dataIndex: 'baseSalary',
      width: 120,
      render: (value) => formatCurrency(value),
    },
    {
      title: '补贴奖金',
      key: 'allowanceGroup',
      width: 180,
      render: (_value, record) => (
        <Space direction="vertical" size={0}>
          <Text>补贴：{formatCurrency(record.allowance)}</Text>
          <Text>绩效：{formatCurrency(record.performanceBonus)}</Text>
          <Text>加班：{formatCurrency(record.overtimePay)}</Text>
        </Space>
      ),
    },
    {
      title: '应发合计',
      dataIndex: 'grossSalary',
      width: 120,
      render: (value) => <Text strong>{formatCurrency(value)}</Text>,
    },
    {
      title: '社保/公积金',
      key: 'deductionGroup',
      width: 180,
      render: (_value, record) => (
        <Space direction="vertical" size={0}>
          <Text>社保：{formatCurrency(record.socialInsurance)}</Text>
          <Text>公积金：{formatCurrency(record.housingFund)}</Text>
        </Space>
      ),
    },
    {
      title: '个税',
      dataIndex: 'incomeTax',
      width: 110,
      render: (value) => <Text type="danger">{formatCurrency(value)}</Text>,
    },
    {
      title: '实发工资',
      dataIndex: 'netSalary',
      width: 120,
      render: (value) => <Text strong style={{ color: '#1677ff' }}>{formatCurrency(value)}</Text>,
    },
    {
      title: '状态/预警',
      key: 'warning',
      width: 180,
      render: (_value, record) => (
        <Space direction="vertical" size={4}>
          {renderWarningTag(record.warningLevel)}
          <Text type="secondary">{record.warningReason || '无异常'}</Text>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 90,
      render: (_value, record) =>
        canManage && currentBatch?.batchStatus === 'PENDING_REVIEW' ? (
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openAdjustmentModal(record)}>
            调整
          </Button>
        ) : (
          <Text type="secondary">--</Text>
        ),
    },
  ];

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            月度薪资核算
          </Title>
          <Text type="secondary">
            管理和审核每月员工薪资发放批次，支持预览、人工调整、重新计算与提交审批。
          </Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Row gutter={[16, 16]} justify="space-between" align="middle">
          <Col xs={24} lg={8}>
            <Space direction="vertical" size={6} style={{ width: '100%' }}>
              <Text type="secondary">核算月份</Text>
              <DatePicker
                picker="month"
                allowClear={false}
                value={dayjs(month, 'YYYY-MM')}
                onChange={(value) => {
                  if (!value) {
                    return;
                  }
                  setMonth(value.format('YYYY-MM'));
                }}
                style={{ width: '100%' }}
              />
            </Space>
          </Col>
          <Col xs={24} lg="auto">
            <Space wrap>
              {!currentBatch ? (
                <Button
                  type="primary"
                  icon={<CalculatorOutlined />}
                  loading={creating}
                  onClick={() => void handleCreateBatch()}
                >
                  新建核算批次
                </Button>
              ) : null}
              {canRecalculate ? (
                <Button
                  icon={<ReloadOutlined />}
                  loading={recalculating}
                  onClick={() => void handleRecalculate()}
                >
                  重新计算
                </Button>
              ) : null}
              {canSubmitApproval ? (
                <Button
                  type="primary"
                  icon={<FileDoneOutlined />}
                  loading={submittingApproval}
                  onClick={() => void handleSubmitApproval()}
                >
                  提交审批
                </Button>
              ) : null}
              {currentBatch?.approvalInstanceId ? (
                <Button
                  icon={<EyeOutlined />}
                  onClick={() => history.push(`/approval/detail/${currentBatch.approvalInstanceId}`)}
                >
                  查看审批
                </Button>
              ) : null}
            </Space>
          </Col>
        </Row>
      </Card>

      <Spin spinning={loadingCurrent || loadingPreview}>
        {currentBatch ? (
          <>
            <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
              <Steps
                current={getBatchStatusIndex(currentBatch.batchStatus)}
                items={BATCH_STATUS_STEPS.map((item) => ({ title: item.title }))}
              />
            </Card>

            <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
              <Col xs={24} md={12} xl={6}>
                <Card bordered={false} style={{ borderRadius: 20 }}>
                  <Space direction="vertical" size={8}>
                    <Text type="secondary">参与核算人数</Text>
                    <Title level={3} style={{ margin: 0, color: '#1677ff' }}>
                      {summary.totalCount} 人
                    </Title>
                  </Space>
                </Card>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Card bordered={false} style={{ borderRadius: 20 }}>
                  <Space direction="vertical" size={8}>
                    <Text type="secondary">应发总额</Text>
                    <Title level={3} style={{ margin: 0, color: '#52c41a' }}>
                      {formatCurrency(summary.totalGrossSalary)}
                    </Title>
                  </Space>
                </Card>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Card bordered={false} style={{ borderRadius: 20 }}>
                  <Space direction="vertical" size={8}>
                    <Text type="secondary">实发总额</Text>
                    <Title level={3} style={{ margin: 0, color: '#722ed1' }}>
                      {formatCurrency(summary.totalNetSalary)}
                    </Title>
                  </Space>
                </Card>
              </Col>
              <Col xs={24} md={12} xl={6}>
                <Card bordered={false} style={{ borderRadius: 20 }}>
                  <Space direction="vertical" size={8}>
                    <Text type="secondary">异常标记</Text>
                    <Title level={3} style={{ margin: 0, color: summary.warnings > 0 ? '#fa541c' : '#13c2c2' }}>
                      {summary.warnings} 条
                    </Title>
                  </Space>
                </Card>
              </Col>
            </Row>

            <Card
              bordered={false}
              style={{ marginBottom: 20, borderRadius: 20 }}
              title="核算预览"
              extra={
                <Space>
                  <Button
                    type={onlyAbnormalPreview ? 'primary' : 'default'}
                    onClick={() => {
                      setOnlyAbnormalPreview((previous) => !previous);
                      setPreviewPageNum(1);
                    }}
                  >
                    异常薪资
                  </Button>
                  <Select
                    allowClear
                    showSearch
                    loading={departmentLoading}
                    placeholder="按部门名称查询"
                    optionFilterProp="label"
                    style={{ width: 220 }}
                    value={selectedDeptName}
                    options={departmentOptions.map((item) => ({
                      label: item.deptName,
                      value: item.deptName,
                    }))}
                    onChange={(value) => {
                      setSelectedDeptName(value);
                      setPreviewPageNum(1);
                    }}
                  />
                  {toNumber(currentBatch.blockCount) > 0 ? (
                    <Tag color="error">存在阻断异常，需处理后才能提交审批</Tag>
                  ) : null}
                  {currentBatch.batchStatus === 'CALCULATING' ? (
                    <Tag color="processing">正在计算中，页面会自动刷新</Tag>
                  ) : null}
                </Space>
              }
            >
              <Table<SalaryBatchItem>
                rowKey={(record) => String(record.id || `${record.batchId}-${record.employeeId}`)}
                columns={columns}
                dataSource={pagedPreviewItems}
                scroll={{ x: 1480 }}
                pagination={{
                  current: previewPageNum,
                  pageSize: previewPageSize,
                  total: filteredPreviewItems.length,
                  showSizeChanger: true,
                  showTotal: (total) => `共 ${total} 条`,
                  onChange: (page, pageSize) => {
                    setPreviewPageNum(page);
                    setPreviewPageSize(pageSize);
                  },
                }}
                onRow={(record) => ({
                  style:
                    record.warningLevel === 'BLOCK'
                      ? { background: '#fff2f0' }
                      : record.warningLevel === 'RED'
                        ? { background: '#fff7e6' }
                        : record.warningLevel === 'YELLOW'
                          ? { background: '#fffbe6' }
                          : undefined,
                })}
              />
            </Card>

            {canManage ? (
              <>
                <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
                  <Col xs={24} xl={12}>
                    <Card bordered={false} style={{ borderRadius: 20 }} title="月度薪资趋势">
                      {loadingTrend ? (
                        <Spin />
                      ) : trendChartData.length > 0 ? (
                        <Line
                          height={260}
                          data={trendChartData}
                          xField="month"
                          yField="amount"
                          colorField="type"
                          point={{ size: 4, shape: 'circle' }}
                          yAxis={{ label: { formatter: (value: string) => `¥${Number(value).toLocaleString('zh-CN')}` } }}
                          tooltip={{
                            items: [
                              (datum: { type: string; amount: number }) => ({
                                name: datum.type,
                                value: formatCurrency(datum.amount),
                              }),
                              (datum: { employeeCount: number }) => ({
                                name: '核算人数',
                                value: `${datum.employeeCount} 人`,
                              }),
                            ],
                          }}
                          smooth
                        />
                      ) : (
                        <Empty description="暂无月度趋势数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                      )}
                    </Card>
                  </Col>
                  <Col xs={24} xl={12}>
                    <Card bordered={false} style={{ borderRadius: 20 }} title="部门薪资分布">
                      {deptChartData.length > 0 ? (
                        <Column
                          height={260}
                          data={deptChartData}
                          xField="deptName"
                          yField="grossSalary"
                          color="#7c4dff"
                          yAxis={{ label: { formatter: (value: string) => `¥${Number(value).toLocaleString('zh-CN')}` } }}
                          tooltip={{
                            items: [
                              (datum: { grossSalary: number }) => ({
                                name: '应发总额',
                                value: formatCurrency(datum.grossSalary),
                              }),
                              (datum: { netSalary: number }) => ({
                                name: '实发总额',
                                value: formatCurrency(datum.netSalary),
                              }),
                              (datum: { employeeCount: number }) => ({
                                name: '人数',
                                value: `${datum.employeeCount} 人`,
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

                <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
                  <Col xs={24} xl={12}>
                    <Card bordered={false} style={{ borderRadius: 20 }} title="薪资构成占比">
                      {compositionData.length > 0 ? (
                        <Space direction="vertical" size={12} style={{ width: '100%' }}>
                          <Space wrap size={[16, 8]} style={{ paddingTop: 4 }}>
                            {compositionData.map((item) => (
                              <Space key={item.type} size={8}>
                                <span
                                  style={{
                                    width: 10,
                                    height: 10,
                                    borderRadius: 2,
                                    display: 'inline-block',
                                    background: item.color,
                                  }}
                                />
                                <Text>{item.type}</Text>
                              </Space>
                            ))}
                          </Space>
                          <div style={{ position: 'relative', height: 260 }}>
                          <Pie
                            height={260}
                            data={compositionData}
                            angleField="amount"
                            colorField="type"
                            color={compositionData.map((item) => item.color)}
                            innerRadius={0.68}
                            radius={0.78}
                            legend={false}
                            label={{
                              text: 'labelText',
                              position: 'spider',
                              connectorDistance: (datum: { percent: number }) =>
                                datum.percent < 0.06 ? 28 : datum.percent < 0.12 ? 20 : 12,
                              transform: [{ type: 'overlapDodgeY' }],
                              style: {
                                fontWeight: 600,
                                fontSize: 12,
                                lineHeight: 14,
                              },
                            }}
                            tooltip={{
                              items: [
                                (datum: { type: string; amount: number; percentText: string }) => ({
                                  name: datum.type,
                                  value: `${formatCurrency(datum.amount)} (${datum.percentText})`,
                                }),
                              ],
                            }}
                          />
                          </div>
                        </Space>
                      ) : (
                        <Empty description="暂无薪资构成数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                      )}
                    </Card>
                  </Col>
                  <Col xs={24} xl={12}>
                    <Card bordered={false} style={{ borderRadius: 20 }} title="社保公积金对比">
                      {socialFundData.length > 0 ? (
                        <Column
                          height={260}
                          data={socialFundData}
                          xField="type"
                          yField="amount"
                          color="#fa8c16"
                          yAxis={{ label: { formatter: (value: string) => `¥${Number(value).toLocaleString('zh-CN')}` } }}
                          tooltip={{
                            items: [
                              (datum: { type: string; amount: number }) => ({
                                name: datum.type,
                                value: formatCurrency(datum.amount),
                              }),
                            ],
                          }}
                        />
                      ) : (
                        <Empty description="暂无社保公积金数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                      )}
                    </Card>
                  </Col>
                </Row>
              </>
            ) : null}

            <Card bordered={false} style={{ borderRadius: 20 }}>
              <Row gutter={[16, 16]} justify="space-between" align="middle">
                <Col xs={24} lg={14}>
                  <Descriptions size="small" column={2}>
                    <Descriptions.Item label="当前批次">{currentBatch.batchNo || '--'}</Descriptions.Item>
                    <Descriptions.Item label="薪资月份">{currentBatch.salaryMonth || month}</Descriptions.Item>
                    <Descriptions.Item label="批次状态">{currentBatch.batchStatus || '--'}</Descriptions.Item>
                    <Descriptions.Item label="审批实例">
                      {currentBatch.approvalInstanceId || '--'}
                    </Descriptions.Item>
                  </Descriptions>
                </Col>
                <Col xs={24} lg="auto">
                  <Space wrap>
                    {canManage && currentBatch.batchStatus === 'PENDING_REVIEW' ? (
                      <Button
                        icon={<EditOutlined />}
                        disabled={!previewData?.items?.length}
                        onClick={() => previewData?.items?.[0] && openAdjustmentModal(previewData.items[0])}
                      >
                        手动调整
                      </Button>
                    ) : null}
                    {canSubmitApproval ? (
                      <Button
                        type="primary"
                        icon={<CheckCircleOutlined />}
                        loading={submittingApproval}
                        onClick={handleSubmitApprovalAction}
                      >
                        提交审批
                      </Button>
                    ) : null}
                  </Space>
                </Col>
              </Row>
            </Card>
          </>
        ) : (
          <Card bordered={false} style={{ borderRadius: 20 }}>
            <Empty
              description={`当前未找到 ${month} 的薪资核算批次`}
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            >
              <Button
                type="primary"
                icon={<CalculatorOutlined />}
                loading={creating}
                onClick={() => void handleCreateBatch()}
              >
                新建核算批次
              </Button>
            </Empty>
          </Card>
        )}
      </Spin>

      <Modal
        title="人工调整"
        open={adjustmentModalOpen}
        onCancel={closeAdjustmentModal}
        onOk={() => void handleSaveAdjustments()}
        confirmLoading={adjustmentSubmitting}
        width={720}
        destroyOnClose
      >
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Card bordered={false} style={{ background: '#fafafa' }}>
            <Descriptions size="small" column={3}>
              <Descriptions.Item label="工号">{adjustingItem?.employeeNo || '--'}</Descriptions.Item>
              <Descriptions.Item label="姓名">{adjustingItem?.employeeName || '--'}</Descriptions.Item>
              <Descriptions.Item label="部门">{adjustingItem?.deptName || '--'}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Form
            form={adjustmentForm}
            layout="vertical"
            initialValues={{
              adjustments: [
                {
                  itemCode: 'ALLOWANCE',
                  adjustAmount: 0,
                  reason: '',
                },
              ],
            }}
          >
            <Form.List name="adjustments">
              {(fields, { add, remove }) => (
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {fields.map((field) => (
                    <Card
                      key={field.key}
                      size="small"
                      title="调整项"
                      extra={
                        fields.length > 1 ? (
                          <Button type="link" danger onClick={() => remove(field.name)}>
                            删除
                          </Button>
                        ) : null
                      }
                    >
                      <Row gutter={12}>
                        <Col span={8}>
                          <Form.Item
                            {...field}
                            label="项目编码"
                            name={[field.name, 'itemCode']}
                            rules={[{ required: true, message: '请选择项目编码' }]}
                          >
                            <Select
                              showSearch
                              options={ADJUSTMENT_ITEM_OPTIONS}
                              placeholder="请选择薪资项目"
                              optionFilterProp="label"
                            />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item
                            {...field}
                            label="调整金额"
                            name={[field.name, 'adjustAmount']}
                            rules={[{ required: true, message: '请输入调整金额' }]}
                          >
                            <InputNumber
                              style={{ width: '100%' }}
                              precision={2}
                              placeholder="可正可负"
                            />
                          </Form.Item>
                        </Col>
                        <Col span={10}>
                          <Form.Item
                            {...field}
                            label="调整原因"
                            name={[field.name, 'reason']}
                            rules={[{ required: true, message: '请输入调整原因' }]}
                          >
                            <Input placeholder="请输入本次调整原因" maxLength={100} />
                          </Form.Item>
                        </Col>
                      </Row>
                    </Card>
                  ))}

                  <Button
                    type="dashed"
                    icon={<EditOutlined />}
                    onClick={() =>
                      add({
                        itemCode: 'ALLOWANCE',
                        adjustAmount: 0,
                        reason: '',
                      })
                    }
                    block
                  >
                    添加调整项
                  </Button>
                </Space>
              )}
            </Form.List>
          </Form>
        </Space>
      </Modal>
    </PageContainer>
  );
};

export default SalaryBatchPage;

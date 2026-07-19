/**
 * 我的薪资页面 — 个人数据中心
 * 上下流式布局：顶部标题 → 折线面积图 → 卡片列表
 */
import {
  getPayslipDetail,
  getPayslipPage,
  getSalaryTrend,
  verifyPayslip,
  type SalaryPayslipDetail,
  type SalaryPayslipListItem,
  type SalaryTrendItem,
} from '@/services/salary';
import type { PageResult } from '@/types/api';
import {
  FileTextOutlined,
  LockOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Area } from '@ant-design/charts';
import {
  Alert,
  Button,
  Card,
  Descriptions,
  Empty,
  Form,
  Input,
  Modal,
  Pagination,
  Space,
  Spin,
  Typography,
  message,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
const { Text, Title } = Typography;
const { Password } = Input;

const STORAGE_PREFIX = 'profile-salary-query';
const DEFAULT_PAGE_SIZE = 5;

interface SalaryQueryState {
  pageNum: number;
  pageSize: number;
}

function getStoredQuery(): SalaryQueryState {
  const storedText = sessionStorage.getItem(STORAGE_PREFIX);
  if (!storedText) {
    return { pageNum: 1, pageSize: DEFAULT_PAGE_SIZE };
  }

  try {
    const stored = JSON.parse(storedText) as Partial<SalaryQueryState>;
    return {
      pageNum: stored.pageNum || 1,
      pageSize: stored.pageSize || DEFAULT_PAGE_SIZE,
    };
  } catch {
    sessionStorage.removeItem(STORAGE_PREFIX);
    return { pageNum: 1, pageSize: DEFAULT_PAGE_SIZE };
  }
}

function toNumber(value?: number | string | null) {
  if (typeof value === 'number') return value;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatCurrency(value?: number | string | null) {
  return `¥${toNumber(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatMonth(value?: string) {
  if (!value) return '--';
  const parsed = dayjs(value, 'YYYY-MM');
  return parsed.isValid() ? parsed.format('YYYY年MM月') : value;
}

function buildIncomeRows(detail?: SalaryPayslipDetail) {
  return [
    { label: '基本工资', amount: detail?.baseSalary },
    { label: '岗位津贴 / 补贴', amount: detail?.allowance },
    { label: '绩效奖金', amount: detail?.performanceBonus },
    { label: '加班费', amount: detail?.overtimePay },
  ];
}

function buildDeductionRows(detail?: SalaryPayslipDetail) {
  return [
    { label: '个人所得税', amount: detail?.incomeTax },
    { label: '养老保险', amount: detail?.pensionInsurance },
    { label: '医疗保险', amount: detail?.medicalInsurance },
    { label: '失业保险', amount: detail?.unemploymentInsurance },
    { label: '住房公积金', amount: detail?.housingFund },
    { label: '迟到扣款', amount: detail?.lateDeduction },
    { label: '请假扣款', amount: detail?.leaveDeduction },
  ];
}

// @ant-design/charts v2 types omit areaStyle/line/point props
const AreaChart = Area as any;
// @ant-design/icons v5.0.1 types missing onPointerEnterCapture in React 18 — suppress
// @ts-ignore
const FileIcon = () => <FileTextOutlined />;
// @ts-ignore
const LockIcon = () => <LockOutlined />;
// @ts-ignore
const ReloadIcon = () => <ReloadOutlined />;

const ProfileSalaryPage: React.FC = () => {
  const [verifyForm] = Form.useForm<{ password: string }>();
  const storedQuery = getStoredQuery();
  const [pageNum, setPageNum] = useState(storedQuery.pageNum);
  const [pageSize, setPageSize] = useState(storedQuery.pageSize);
  const [payslipPageData, setPayslipPageData] = useState<PageResult<SalaryPayslipListItem>>();
  const [trendData, setTrendData] = useState<SalaryTrendItem[]>([]);
  const [loadingList, setLoadingList] = useState(false);
  const [loadingTrend, setLoadingTrend] = useState(false);
  const [verifyModalOpen, setVerifyModalOpen] = useState(false);
  const [verifySubmitting, setVerifySubmitting] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailData, setDetailData] = useState<SalaryPayslipDetail>();
  const [pendingPayslip, setPendingPayslip] = useState<SalaryPayslipListItem>();

  const loadTrend = async () => {
    setLoadingTrend(true);
    try {
      const nextTrend = await getSalaryTrend();
      setTrendData(nextTrend || []);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '薪资趋势加载失败';
      message.error(messageText);
    } finally {
      setLoadingTrend(false);
    }
  };

  const loadPayslipPage = async (
    nextPageNum = pageNum,
    nextPageSize = pageSize,
  ) => {
    setLoadingList(true);
    try {
      const nextPage = await getPayslipPage({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setPayslipPageData(nextPage);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '工资条列表加载失败';
      message.error(messageText);
    } finally {
      setLoadingList(false);
    }
  };

  const loadPageData = async () => {
    await Promise.all([loadTrend(), loadPayslipPage()]);
  };

  const openPayslipDetail = async (record: SalaryPayslipListItem) => {
    setDetailModalOpen(true);
    setDetailLoading(true);
    setDetailData(undefined);
    try {
      const detail = await getPayslipDetail(record.id);
      setDetailData(detail);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '工资条详情加载失败';
      message.error(messageText);
      setDetailModalOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    void loadPageData();
  }, []);

  useEffect(() => {
    sessionStorage.setItem(STORAGE_PREFIX, JSON.stringify({ pageNum, pageSize }));
  }, [pageNum, pageSize]);

  const handleReload = () => {
    void loadPageData();
  };

  const handleView = async (record: SalaryPayslipListItem) => {
    setPendingPayslip(record);
    if (record.verified) {
      await openPayslipDetail(record);
      return;
    }
    verifyForm.resetFields();
    setVerifyModalOpen(true);
  };

  const handleVerify = async () => {
    if (!pendingPayslip) return;
    try {
      const values = await verifyForm.validateFields();
      setVerifySubmitting(true);
      await verifyPayslip({
        month: pendingPayslip.salaryMonth,
        password: values.password,
      });
      message.success('验证通过');
      setVerifyModalOpen(false);
      verifyForm.resetFields();
      await loadPayslipPage(pageNum, pageSize);
      await openPayslipDetail(pendingPayslip);
    } catch (error) {
      if (error instanceof Error) message.error(error.message);
    } finally {
      setVerifySubmitting(false);
    }
  };

  const incomeRows = useMemo(() => buildIncomeRows(detailData), [detailData]);
  const deductionRows = useMemo(() => buildDeductionRows(detailData), [detailData]);

  return (
    <div style={{ padding: 24, maxWidth: 1000, margin: '0 auto' }}>
      {/* ===== 1. 顶部标题区 ===== */}
      <div style={{ marginBottom: 24 }}>
        <Title level={2} style={{ margin: 0, fontWeight: 600 }}>
          我的薪资
        </Title>
        <Text type="secondary" style={{ marginTop: 4, display: 'block' }}>
          查看薪资记录与趋势分析
        </Text>
      </div>

      {/* ===== 2. 图表卡片区 — 折线面积图 ===== */}
      <Card
        bordered={false}
        title="近6个月实发工资趋势"
        style={{ marginBottom: 24, borderRadius: 8 }}
      >
        {loadingTrend ? (
          <div style={{ textAlign: 'center', padding: 40 }}>
            <Spin />
          </div>
        ) : trendData.length > 0 ? (
          <AreaChart
            height={280}
            data={trendData}
            xField="month"
            yField="netSalary"
            shape="smooth"
            color="#5B8FF9"
            line={{ color: '#5B8FF9', size: 2, shape: 'smooth' }}
            point={{ size: 4, shape: 'circle', color: '#5B8FF9' }}
            style={{
              fill: 'l(270) 0:rgba(91,143,249,0.18) 1:rgba(91,143,249,0.01)',
            }}
            xAxis={{
              label: {
                formatter: (value: string) => value,
              },
            }}
            yAxis={{
              label: {
                formatter: (value: string) => {
                  const num = Number(value);
                  if (num >= 1000) return `¥${(num / 1000).toFixed(0)}k`;
                  return `¥${num}`;
                },
              },
              nice: true,
              grid: {
                line: {
                  style: {
                    stroke: '#e8e8e8',
                    lineWidth: 1,
                    lineDash: [4, 4],
                  },
                },
              },
            }}
            tooltip={{
              items: [
                (datum: SalaryTrendItem) => ({
                  name: '实发工资',
                  value: formatCurrency(datum.netSalary),
                }),
              ],
            }}
          />
        ) : (
          <Empty description="暂无薪资趋势数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Card>

      {/* ===== 3. 工资条列表区 — 卡片列表 ===== */}
      <Card
        bordered={false}
        style={{ borderRadius: 8 }}
        title="工资条列表"
        extra={
          <Button icon={<ReloadIcon />} onClick={handleReload}>
            刷新
          </Button>
        }
      >
        <Spin spinning={loadingList}>
          {(payslipPageData?.records?.length ?? 0) > 0 ? (
            <>
              {/* 卡片列表 */}
              {(payslipPageData?.records ?? []).map((item) => (
                <Card
                  key={item.id}
                  style={{
                    marginBottom: 12,
                    borderRadius: 8,
                    boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
                    border: '1px solid #f0f0f0',
                  }}
                  styles={{ body: { padding: '16px 20px' } }}
                >
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 16,
                    }}
                  >
                    {/* 左侧图标占位 */}
                    <div
                      style={{
                        width: 48,
                        height: 48,
                        borderRadius: 8,
                        background: '#e6f4ff',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      <span style={{ fontSize: 22, color: '#1677ff' }}>
                        <FileIcon />
                      </span>
                    </div>

                    {/* 中间信息 */}
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <Text strong style={{ fontSize: 16, display: 'block' }}>
                        {formatMonth(item.salaryMonth)}工资条
                      </Text>
                      <Text type="secondary" style={{ fontSize: 13 }}>
                        基本工资 + 绩效奖金
                      </Text>
                    </div>

                    {/* 右侧金额 */}
                    <div style={{ textAlign: 'right', flexShrink: 0, marginRight: 8 }}>
                      <Text strong style={{ fontSize: 20, color: '#1677ff', display: 'block' }}>
                        {formatCurrency(item.netSalary)}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        实发工资
                      </Text>
                    </div>

                    {/* 操作按钮 */}
                    <Button
                      type="primary"
                      ghost
                      style={{ flexShrink: 0 }}
                      onClick={() => void handleView(item)}
                    >
                      查看详情
                    </Button>
                  </div>
                </Card>
              ))}

              {/* 分页 */}
              <div style={{ textAlign: 'right', marginTop: 16 }}>
                <Pagination
                  current={payslipPageData?.pageNum || pageNum}
                  pageSize={payslipPageData?.pageSize || pageSize}
                  total={payslipPageData?.total || 0}
                  showTotal={(total) => `共 ${total} 条`}
                  onChange={(nextPageNum, nextPageSize) => {
                    setPageNum(nextPageNum);
                    setPageSize(nextPageSize);
                    void loadPayslipPage(nextPageNum, nextPageSize);
                  }}
                />
              </div>
            </>
          ) : (
            <Empty description="暂无工资条数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </Spin>
      </Card>

      {/* ===== 二次验证弹窗 ===== */}
      <Modal
        title="二次验证"
        open={verifyModalOpen}
        onCancel={() => {
          setVerifyModalOpen(false);
          verifyForm.resetFields();
        }}
        onOk={() => void handleVerify()}
        confirmLoading={verifySubmitting}
        okText="验证并查看"
        cancelText="取消"
        width={420}
        destroyOnClose
      >
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Alert
            type="info"
            showIcon
            message="工资条属于敏感信息，请输入登录密码完成二次验证。"
          />
          <Form form={verifyForm} layout="vertical">
            <Form.Item
              label="登录密码"
              name="password"
              rules={[{ required: true, message: '请输入登录密码' }]}
            >
              <Password
                prefix={<span><LockIcon /></span>}
                placeholder="请输入登录密码"
                autoComplete="current-password"
              />
            </Form.Item>
          </Form>
        </Space>
      </Modal>

      {/* ===== 工资条详情弹窗 ===== */}
      <Modal
        title="工资条详情"
        open={detailModalOpen}
        footer={null}
        onCancel={() => {
          setDetailModalOpen(false);
          setDetailData(undefined);
        }}
        width={920}
        destroyOnClose
      >
        <Spin spinning={detailLoading}>
          <Alert
            type="success"
            showIcon
            message="已完成二次验证"
            style={{ marginBottom: 16 }}
          />

          <Descriptions bordered column={{ xs: 1, sm: 2, md: 3 }} size="small" style={{ marginBottom: 16 }}>
            <Descriptions.Item label="工资月份">
              {formatMonth(detailData?.salaryMonth)}
            </Descriptions.Item>
            <Descriptions.Item label="发放日期">--</Descriptions.Item>
            <Descriptions.Item label="工资批次">{detailData?.batchNo || '--'}</Descriptions.Item>
            <Descriptions.Item label="员工姓名">{detailData?.employeeName || '--'}</Descriptions.Item>
            <Descriptions.Item label="工号">{detailData?.employeeNo || '--'}</Descriptions.Item>
            <Descriptions.Item label="部门">{detailData?.deptName || '--'}</Descriptions.Item>
          </Descriptions>

          <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
            {/* 收入明细 */}
            <Card
              bordered={false}
              style={{ flex: 1, minWidth: 280, background: '#f6ffed' }}
              title="收入明细"
            >
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                {incomeRows.map((item) => (
                  <div key={item.label} style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text>{item.label}</Text>
                    <Text strong>{formatCurrency(item.amount)}</Text>
                  </div>
                ))}
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    paddingTop: 8,
                    borderTop: '1px solid #d9f7be',
                  }}
                >
                  <Text strong>应发小计</Text>
                  <Text strong style={{ color: '#389e0d' }}>
                    {formatCurrency(detailData?.grossSalary)}
                  </Text>
                </div>
              </Space>
            </Card>

            {/* 扣除明细 */}
            <Card
              bordered={false}
              style={{ flex: 1, minWidth: 280, background: '#fff7e6' }}
              title="扣除明细"
            >
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                {deductionRows.map((item) => (
                  <div key={item.label} style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text>{item.label}</Text>
                    <Text strong type="danger">
                      -{formatCurrency(item.amount)}
                    </Text>
                  </div>
                ))}
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    paddingTop: 8,
                    borderTop: '1px solid #ffe7ba',
                  }}
                >
                  <Text strong>应扣小计</Text>
                  <Text strong type="danger">
                    -{formatCurrency(detailData?.deductionTotal)}
                  </Text>
                </div>
              </Space>
            </Card>
          </div>

          <Card
            bordered={false}
            style={{
              marginTop: 16,
              background: '#e6f4ff',
              textAlign: 'center',
            }}
          >
            <Space direction="vertical" size={4}>
              <Text type="secondary">实发工资</Text>
              <Title level={1} style={{ margin: 0, color: '#1677ff' }}>
                {formatCurrency(detailData?.netSalary)}
              </Title>
            </Space>
          </Card>
        </Spin>
      </Modal>
    </div>
  );
};

export default ProfileSalaryPage;

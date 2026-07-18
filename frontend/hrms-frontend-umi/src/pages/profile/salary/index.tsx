/**
 * 我的薪资页面
 * 在个人中心内展示员工端工资趋势、工资条列表、二次验证和工资条详情。
 */
import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
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
  EyeOutlined,
  LockOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import { Line } from '@ant-design/charts';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Descriptions,
  Empty,
  Form,
  Input,
  Modal,
  Row,
  Space,
  Spin,
  Statistic,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Password } = Input;
const { Text, Title } = Typography;

const STORAGE_PREFIX = 'profile-salary-query';
const DEFAULT_PAGE_SIZE = 10;

interface SalaryQueryState {
  selectedMonth: string;
  pageNum: number;
  pageSize: number;
}

function resolveStorageKey() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return `${STORAGE_PREFIX}:anonymous`;
  }

  try {
    const userInfo = JSON.parse(userInfoText) as {
      userId?: number | string;
      id?: number | string;
      username?: string;
    };
    const identity = userInfo.userId || userInfo.id || userInfo.username || 'anonymous';
    return `${STORAGE_PREFIX}:${identity}`;
  } catch {
    return `${STORAGE_PREFIX}:anonymous`;
  }
}

function getStoredQuery(): SalaryQueryState {
  const storedText = sessionStorage.getItem(resolveStorageKey());
  if (!storedText) {
    return {
      selectedMonth: dayjs().format('YYYY-MM'),
      pageNum: 1,
      pageSize: DEFAULT_PAGE_SIZE,
    };
  }

  try {
    const stored = JSON.parse(storedText) as Partial<SalaryQueryState>;
    return {
      selectedMonth: stored.selectedMonth || dayjs().format('YYYY-MM'),
      pageNum: stored.pageNum || 1,
      pageSize: stored.pageSize || DEFAULT_PAGE_SIZE,
    };
  } catch {
    sessionStorage.removeItem(resolveStorageKey());
    return {
      selectedMonth: dayjs().format('YYYY-MM'),
      pageNum: 1,
      pageSize: DEFAULT_PAGE_SIZE,
    };
  }
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
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatMonth(value?: string) {
  if (!value) {
    return '--';
  }
  const parsed = dayjs(value, 'YYYY-MM');
  return parsed.isValid() ? parsed.format('YYYY年MM月') : value;
}

function renderBatchStatus(status?: string) {
  const statusMap: Record<string, { text: string; color: string }> = {
    DRAFT: { text: '草稿', color: 'default' },
    CALCULATING: { text: '核算中', color: 'processing' },
    PENDING_REVIEW: { text: '待确认', color: 'warning' },
    APPROVING: { text: '审批中', color: 'processing' },
    APPROVED: { text: '已通过', color: 'success' },
    RELEASED: { text: '已发放', color: 'success' },
    ARCHIVED: { text: '已归档', color: 'default' },
  };
  const item = status ? statusMap[status] : undefined;
  return <Tag color={item?.color || 'default'}>{item?.text || status || '--'}</Tag>;
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

const ProfileSalaryPage: React.FC = () => {
  const [verifyForm] = Form.useForm<{ password: string }>();
  const storedQuery = getStoredQuery();
  const [selectedMonth, setSelectedMonth] = useState(storedQuery.selectedMonth);
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
    nextMonth = selectedMonth,
    nextPageNum = pageNum,
    nextPageSize = pageSize,
  ) => {
    setLoadingList(true);
    try {
      const nextPage = await getPayslipPage({
        month: nextMonth,
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
    sessionStorage.setItem(
      resolveStorageKey(),
      JSON.stringify({
        selectedMonth,
        pageNum,
        pageSize,
      }),
    );
  }, [pageNum, pageSize, selectedMonth]);

  usePageAutoRefresh(() => {
    void loadPageData();
  });

  const handleMonthChange = (value: Dayjs | null) => {
    if (!value) {
      return;
    }
    const nextMonth = value.format('YYYY-MM');
    setSelectedMonth(nextMonth);
    setPageNum(1);
    void loadPayslipPage(nextMonth, 1, pageSize);
  };

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
    if (!pendingPayslip) {
      return;
    }
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
      await loadPayslipPage(selectedMonth, pageNum, pageSize);
      await openPayslipDetail(pendingPayslip);
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setVerifySubmitting(false);
    }
  };

  const latestPayslip = useMemo(() => {
    const records = payslipPageData?.records || [];
    return records[0];
  }, [payslipPageData?.records]);

  const incomeRows = useMemo(() => buildIncomeRows(detailData), [detailData]);
  const deductionRows = useMemo(() => buildDeductionRows(detailData), [detailData]);

  const columns: ColumnsType<SalaryPayslipListItem> = [
    {
      title: '月份',
      dataIndex: 'salaryMonth',
      width: 140,
      render: (value) => <Text strong>{formatMonth(value)}</Text>,
    },
    {
      title: '应发工资',
      dataIndex: 'grossSalary',
      width: 140,
      render: (value) => formatCurrency(value),
    },
    {
      title: '应扣金额',
      dataIndex: 'deductionTotal',
      width: 140,
      render: (value) => <Text type="danger">{formatCurrency(value)}</Text>,
    },
    {
      title: '实发工资',
      dataIndex: 'netSalary',
      width: 140,
      render: (value) => (
        <Text strong style={{ color: '#1677ff' }}>
          {formatCurrency(value)}
        </Text>
      ),
    },
    {
      title: '发放状态',
      dataIndex: 'batchStatus',
      width: 120,
      render: (value) => renderBatchStatus(value),
    },
    {
      title: '验证状态',
      dataIndex: 'verified',
      width: 120,
      render: (value) =>
        value ? <Tag color="success">已验证</Tag> : <Tag color="warning">待验证</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 110,
      fixed: 'right',
      render: (_value, record) => (
        <Button type="link" icon={<EyeOutlined />} onClick={() => void handleView(record)}>
          查看
        </Button>
      ),
    },
  ];

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            我的薪资
          </Title>
          <Text type="secondary">查看近 6 个月实发趋势、工资条列表与薪资明细。</Text>
        </Space>
      }
    >
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} xl={15}>
          <Card bordered={false} title="近 6 个月实发工资趋势" style={{ height: '100%' }}>
            {loadingTrend ? (
              <Spin />
            ) : trendData.length > 0 ? (
              <Line
                height={280}
                data={trendData}
                xField="month"
                yField="netSalary"
                point={{ size: 4, shape: 'circle' }}
                smooth
                yAxis={{
                  label: {
                    formatter: (value: string) => `¥${Number(value).toLocaleString('zh-CN')}`,
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
        </Col>
        <Col xs={24} xl={9}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Card bordered={false}>
              <Space align="start" size={12}>
                <SafetyCertificateOutlined style={{ color: '#1677ff', fontSize: 24 }} />
                <Space direction="vertical" size={4}>
                  <Text strong>薪资数据安全</Text>
                  <Text type="secondary">
                    查看工资条前需要完成密码二次验证，工资条详情每次查看都会实时请求。
                  </Text>
                </Space>
              </Space>
            </Card>

            <Card bordered={false} title="本月薪资概览">
              {latestPayslip ? (
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <Statistic title="应发工资" value={formatCurrency(latestPayslip.grossSalary)} />
                  </Col>
                  <Col span={12}>
                    <Statistic title="实发工资" value={formatCurrency(latestPayslip.netSalary)} />
                  </Col>
                  <Col span={12}>
                    <Space direction="vertical" size={4}>
                      <Text type="secondary">发放状态</Text>
                      {renderBatchStatus(latestPayslip.batchStatus)}
                    </Space>
                  </Col>
                  <Col span={12}>
                    <Space direction="vertical" size={4}>
                      <Text type="secondary">发放日期</Text>
                      <Text>--</Text>
                    </Space>
                  </Col>
                </Row>
              ) : (
                <Empty description="当前月份暂无工资条" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Space>
        </Col>
      </Row>

      <Card
        bordered={false}
        title="工资条列表"
        extra={
          <Space wrap>
            <DatePicker
              picker="month"
              allowClear={false}
              value={dayjs(selectedMonth, 'YYYY-MM')}
              onChange={handleMonthChange}
            />
            <Button icon={<ReloadOutlined />} onClick={handleReload}>
              刷新
            </Button>
          </Space>
        }
      >
        <Table<SalaryPayslipListItem>
          rowKey="id"
          columns={columns}
          dataSource={payslipPageData?.records || []}
          loading={loadingList}
          scroll={{ x: 900 }}
          locale={{ emptyText: '暂无工资条数据' }}
          pagination={{
            current: payslipPageData?.pageNum || pageNum,
            pageSize: payslipPageData?.pageSize || pageSize,
            total: payslipPageData?.total || 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (nextPageNum, nextPageSize) => {
              setPageNum(nextPageNum);
              setPageSize(nextPageSize);
              void loadPayslipPage(selectedMonth, nextPageNum, nextPageSize);
            },
          }}
        />
      </Card>

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
                prefix={<LockOutlined />}
                placeholder="请输入登录密码"
                autoComplete="current-password"
              />
            </Form.Item>
          </Form>
        </Space>
      </Modal>

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

          <Row gutter={[16, 16]}>
            <Col xs={24} md={12}>
              <Card bordered={false} style={{ background: '#f6ffed' }} title="收入明细">
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {incomeRows.map((item) => (
                    <Row key={item.label} justify="space-between">
                      <Text>{item.label}</Text>
                      <Text strong>{formatCurrency(item.amount)}</Text>
                    </Row>
                  ))}
                  <Row justify="space-between" style={{ paddingTop: 8, borderTop: '1px solid #d9f7be' }}>
                    <Text strong>应发小计</Text>
                    <Text strong style={{ color: '#389e0d' }}>
                      {formatCurrency(detailData?.grossSalary)}
                    </Text>
                  </Row>
                </Space>
              </Card>
            </Col>
            <Col xs={24} md={12}>
              <Card bordered={false} style={{ background: '#fff7e6' }} title="扣除明细">
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {deductionRows.map((item) => (
                    <Row key={item.label} justify="space-between">
                      <Text>{item.label}</Text>
                      <Text strong type="danger">
                        -{formatCurrency(item.amount)}
                      </Text>
                    </Row>
                  ))}
                  <Row justify="space-between" style={{ paddingTop: 8, borderTop: '1px solid #ffe7ba' }}>
                    <Text strong>应扣小计</Text>
                    <Text strong type="danger">
                      -{formatCurrency(detailData?.deductionTotal)}
                    </Text>
                  </Row>
                </Space>
              </Card>
            </Col>
          </Row>

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
    </PageContainer>
  );
};

export default ProfileSalaryPage;

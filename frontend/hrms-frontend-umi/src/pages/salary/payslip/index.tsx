import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type { DeptListItem } from '@/services/organization';
import { getDepartmentList } from '@/services/organization';
import {
  getManagePayslipDetail,
  getManagePayslipList,
  verifyManagePayslip,
  type SalaryManagePayslip,
  type SalaryManagePayslipQuery,
  type SalaryPayslipDetail,
} from '@/services/salary';
import {
  EyeOutlined,
  LockOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import type { PageResult } from '@/types/api';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Descriptions,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Password } = Input;
const { Text, Title } = Typography;

const PAYSLIP_QUERY_STORAGE_PREFIX = 'salary-manage-payslip-query';
const DEFAULT_PAGE_SIZE = 10;

const VIEW_STATUS_OPTIONS = [
  { label: '已查看', value: 'VIEWED' },
  { label: '未查看', value: 'UNVIEWED' },
  { label: '待发布', value: 'UNPUBLISHED' },
];

interface PayslipFilterValues {
  keyword?: string;
  month?: Dayjs;
  deptId?: number;
  viewStatus?: 'VIEWED' | 'UNVIEWED' | 'UNPUBLISHED';
}

interface PayslipQueryState {
  keyword?: string;
  month?: string;
  deptId?: number;
  viewStatus?: 'VIEWED' | 'UNVIEWED' | 'UNPUBLISHED';
  pageNum: number;
  pageSize: number;
}

interface StoredPayslipQueryState extends Partial<PayslipQueryState> {}

/** 生成工资条查询缓存键，按用户隔离最近的筛选条件。 */
function resolveStorageKey() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return `${PAYSLIP_QUERY_STORAGE_PREFIX}:anonymous`;
  }

  try {
    const userInfo = JSON.parse(userInfoText) as {
      userId?: number | string;
      id?: number | string;
      username?: string;
    };
    const identity = userInfo.userId || userInfo.id || userInfo.username || 'anonymous';
    return `${PAYSLIP_QUERY_STORAGE_PREFIX}:${identity}`;
  } catch {
    return `${PAYSLIP_QUERY_STORAGE_PREFIX}:anonymous`;
  }
}

/** 读取工资条查询缓存，供页面回显上次筛选状态。 */
function getStoredQuery(): StoredPayslipQueryState {
  const storedText = sessionStorage.getItem(resolveStorageKey());
  if (!storedText) {
    return {};
  }

  try {
    return JSON.parse(storedText) as StoredPayslipQueryState;
  } catch {
    sessionStorage.removeItem(resolveStorageKey());
    return {};
  }
}

/** 格式化工资条金额显示。 */
function formatCurrency(value?: number | string | null) {
  const amount = Number(value || 0);
  return `¥${amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

/** 渲染工资条查看状态标签。 */
function renderViewStatusTag(status?: string) {
  if (status === 'VIEWED') {
    return <Tag color="success">已查看</Tag>;
  }
  if (status === 'UNVIEWED') {
    return <Tag color="processing">未查看</Tag>;
  }
  if (status === 'UNPUBLISHED') {
    return <Tag color="default">待发布</Tag>;
  }
  return <Tag>{status || '--'}</Tag>;
}

/** 格式化工资月份展示文案。 */
function formatMonth(value?: string) {
  if (!value) {
    return '--';
  }
  const date = dayjs(value, 'YYYY-MM');
  return date.isValid() ? date.format('YYYY年MM月') : value;
}

/** 构造工资条查询参数。 */
function buildQueryParams(query: PayslipQueryState): SalaryManagePayslipQuery {
  return {
    keyword: query.keyword,
    month: query.month,
    deptId: query.deptId,
    viewStatus: query.viewStatus,
    pageNum: query.pageNum,
    pageSize: query.pageSize,
  };
}

/** 构造收入项列表，供工资条详情收入分区展示。 */
function buildIncomeItems(detail?: SalaryPayslipDetail) {
  if (!detail) {
    return [];
  }
  return [
    { label: '基本工资', amount: detail.baseSalary },
    { label: '岗位津贴', amount: detail.allowance },
    { label: '绩效奖金', amount: detail.performanceBonus },
    { label: '加班费', amount: detail.overtimePay },
  ].filter((item) => Number(item.amount || 0) !== 0);
}

/** 构造扣款项列表，供工资条详情扣款分区展示。 */
function buildDeductionItems(detail?: SalaryPayslipDetail) {
  if (!detail) {
    return [];
  }
  return [
    { label: '事假扣款', amount: detail.leaveDeduction },
    { label: '迟到扣款', amount: detail.lateDeduction },
    { label: '养老保险', amount: detail.socialInsurance },
    { label: '住房公积金', amount: detail.housingFund },
    { label: '个人所得税', amount: detail.incomeTax },
  ].filter((item) => Number(item.amount || 0) !== 0);
}

/**
 * 管理端工资条页面组件。
 * 负责工资条筛选、密码校验、详情查看和查看状态更新。
 */
const SalaryPayslipManagePage: React.FC = () => {
  const [form] = Form.useForm<PayslipFilterValues>();
  const [verifyForm] = Form.useForm<{ password: string }>();
  const storedQuery = getStoredQuery();
  const [query, setQuery] = useState<PayslipQueryState>({
    keyword: storedQuery.keyword,
    month: storedQuery.month || dayjs().format('YYYY-MM'),
    deptId: storedQuery.deptId,
    viewStatus: storedQuery.viewStatus,
    pageNum: storedQuery.pageNum || 1,
    pageSize: storedQuery.pageSize || DEFAULT_PAGE_SIZE,
  });
  const [departmentOptions, setDepartmentOptions] = useState<DeptListItem[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [tableLoading, setTableLoading] = useState(false);
  const [payslipPageData, setPayslipPageData] = useState<PageResult<SalaryManagePayslip>>();
  const [verifyModalOpen, setVerifyModalOpen] = useState(false);
  const [verifySubmitting, setVerifySubmitting] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailData, setDetailData] = useState<SalaryPayslipDetail>();
  const [pendingViewRecord, setPendingViewRecord] = useState<SalaryManagePayslip>();

  /** 加载部门列表，供工资条筛选中的部门下拉使用。 */
  const loadDepartments = async () => {
    setDepartmentLoading(true);
    try {
      const nextDepartments = await getDepartmentList();
      setDepartmentOptions(nextDepartments || []);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '部门列表加载失败';
      message.error(messageText);
    } finally {
      setDepartmentLoading(false);
    }
  };

  /** 加载工资条列表，内部调用 `buildQueryParams` 统一查询参数。 */
  const loadManagePayslips = async (nextQuery: PayslipQueryState) => {
    setTableLoading(true);
    try {
      const nextPage = await getManagePayslipList(buildQueryParams(nextQuery));
      setPayslipPageData(nextPage);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '工资条列表加载失败';
      message.error(messageText);
    } finally {
      setTableLoading(false);
    }
  };

  const markPayslipViewed = (payslipId: number) => {
    setPayslipPageData((previous) => {
      if (!previous?.records?.length) {
        return previous;
      }
      const nextRecords = previous.records
        .map((record) =>
          record.id === payslipId ? { ...record, viewStatus: 'VIEWED' as const } : record,
        )
        .filter((record) =>
          query.viewStatus === 'UNVIEWED' ? record.id !== payslipId : true,
        );
      return {
        ...previous,
        records: nextRecords,
        total:
          query.viewStatus === 'UNVIEWED'
            ? Math.max((previous.total || 0) - 1, 0)
            : previous.total,
      };
    });
  };

  /** 打开工资条详情，内部查询详情并展示弹窗。 */
  const openPayslipDetail = async (id: number) => {
    setDetailLoading(true);
    try {
      const detail = await getManagePayslipDetail(id);
      setDetailData(detail);
      setDetailModalOpen(true);
      markPayslipViewed(id);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '工资条详情加载失败';
      message.error(messageText);
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    void loadDepartments();
  }, []);

  useEffect(() => {
    form.setFieldsValue({
      keyword: query.keyword,
      month: query.month ? dayjs(query.month, 'YYYY-MM') : undefined,
      deptId: query.deptId,
      viewStatus: query.viewStatus,
    });
  }, [form, query.deptId, query.keyword, query.month, query.viewStatus]);

  useEffect(() => {
    void loadManagePayslips(query);
  }, [query]);

  useEffect(() => {
    sessionStorage.setItem(resolveStorageKey(), JSON.stringify(query));
  }, [query]);

  usePageAutoRefresh(() => {
    void loadDepartments();
    void loadManagePayslips(query);
  });

  /** 执行工资条查询并重置分页。 */
  const handleSearch = (values: PayslipFilterValues) => {
    setQuery((previous) => ({
      ...previous,
      keyword: values.keyword?.trim() || undefined,
      month: values.month?.format('YYYY-MM'),
      deptId: values.deptId,
      viewStatus: values.viewStatus,
      pageNum: 1,
    }));
  };

  /** 重置工资条筛选条件并恢复默认月份。 */
  const handleReset = () => {
    setQuery({
      month: dayjs().format('YYYY-MM'),
      pageNum: 1,
      pageSize: query.pageSize,
    });
  };

  /** 处理查看工资条动作，必要时先进入密码校验流程。 */
  const handleView = async (record: SalaryManagePayslip) => {
    setPendingViewRecord(record);
    if (record.viewStatus === 'UNPUBLISHED') {
      message.warning('该工资条待发布，暂不可查看');
      return;
    }
    if (record.verified) {
      await openPayslipDetail(record.id);
      return;
    }
    verifyForm.resetFields();
    setVerifyModalOpen(true);
  };

  /** 校验查看密码，成功后调用 `openPayslipDetail` 展示工资条详情。 */
  const handleVerify = async () => {
    if (!pendingViewRecord) {
      return;
    }
    try {
      const values = await verifyForm.validateFields();
      setVerifySubmitting(true);
      await verifyManagePayslip({ password: values.password });
      message.success('验证通过');
      setVerifyModalOpen(false);
      await openPayslipDetail(pendingViewRecord.id);
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setVerifySubmitting(false);
    }
  };

  const columns: ColumnsType<SalaryManagePayslip> = [
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      width: 130,
      render: (value) => <Text strong>{value || '--'}</Text>,
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
      width: 130,
      render: (value) => value || '--',
    },
    {
      title: '月份',
      dataIndex: 'salaryMonth',
      width: 120,
      render: (value) => formatMonth(value),
    },
    {
      title: '应发总额',
      dataIndex: 'grossSalary',
      width: 120,
      render: (value) => formatCurrency(value),
    },
    {
      title: '应扣总额',
      dataIndex: 'deductionTotal',
      width: 120,
      render: (value) => <Text type="danger">{formatCurrency(value)}</Text>,
    },
    {
      title: '实发工资',
      dataIndex: 'netSalary',
      width: 120,
      render: (value) => <Text strong style={{ color: '#1677ff' }}>{formatCurrency(value)}</Text>,
    },
    {
      title: '状态',
      dataIndex: 'viewStatus',
      width: 110,
      render: (value) => renderViewStatusTag(value),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 100,
      render: (_value, record) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          disabled={record.viewStatus === 'UNPUBLISHED'}
          onClick={() => void handleView(record)}
        >
          查看
        </Button>
      ),
    },
  ];

  const incomeItems = useMemo(() => buildIncomeItems(detailData), [detailData]);
  const deductionItems = useMemo(() => buildDeductionItems(detailData), [detailData]);

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            工资条管理
          </Title>
          <Text type="secondary">查看和管理员工工资条，支持密码二次验证与查看状态回显。</Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          initialValues={{
            keyword: query.keyword,
            month: query.month ? dayjs(query.month, 'YYYY-MM') : undefined,
            deptId: query.deptId,
            viewStatus: query.viewStatus,
          }}
          style={{ rowGap: 16 }}
        >
          <Form.Item label="员工" name="keyword">
            <Input
              allowClear
              placeholder="搜索员工姓名/工号"
              prefix={<SearchOutlined />}
              style={{ width: 240 }}
            />
          </Form.Item>
          <Form.Item label="月份" name="month">
            <DatePicker picker="month" allowClear />
          </Form.Item>
          <Form.Item label="部门" name="deptId">
            <Select
              allowClear
              showSearch
              loading={departmentLoading}
              optionFilterProp="label"
              placeholder="请选择部门"
              options={departmentOptions.map((item) => ({
                label: item.deptName,
                value: item.id,
              }))}
              style={{ width: 200 }}
            />
          </Form.Item>
          <Form.Item label="状态" name="viewStatus">
            <Select
              allowClear
              placeholder="请选择状态"
              options={VIEW_STATUS_OPTIONS}
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

      <Card bordered={false} style={{ borderRadius: 20 }} title="工资条列表">
        <Table<SalaryManagePayslip>
          rowKey="id"
          columns={columns}
          dataSource={payslipPageData?.records || []}
          loading={tableLoading}
          scroll={{ x: 1080 }}
          pagination={{
            current: payslipPageData?.pageNum || query.pageNum,
            pageSize: payslipPageData?.pageSize || query.pageSize,
            total: payslipPageData?.total || 0,
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
          <Text type="secondary">
            为保障工资信息安全，请先完成身份验证。当前管理者本次登录只需验证一次。
          </Text>
          <Form form={verifyForm} layout="vertical">
            <Form.Item
              label="验证码或密码"
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
        title={null}
        open={detailModalOpen}
        footer={null}
        onCancel={() => {
          setDetailModalOpen(false);
          setDetailData(undefined);
        }}
        width={980}
        destroyOnClose
      >
        <Spin spinning={detailLoading}>
          <div
            style={{
              background: 'linear-gradient(135deg, #2f6bff 0%, #2955d9 100%)',
              borderRadius: 20,
              padding: '28px 32px 24px',
              color: '#fff',
              marginBottom: 20,
            }}
          >
            <Space direction="vertical" size={8} style={{ width: '100%', textAlign: 'center' }}>
              <Title level={2} style={{ color: '#fff', margin: 0 }}>
                HRMS 人资管理系统
              </Title>
              <Text style={{ color: 'rgba(255,255,255,0.88)', fontSize: 18 }}>
                {detailData?.salaryMonth || '--'} 工资条
              </Text>
            </Space>

            <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
              <Col xs={24} md={8}>
                <Descriptions column={1} size="small" styles={{ label: { color: 'rgba(255,255,255,0.7)' }, content: { color: '#fff', fontWeight: 600 } }}>
                  <Descriptions.Item label="员工姓名">{detailData?.employeeName || '--'}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col xs={24} md={8}>
                <Descriptions column={1} size="small" styles={{ label: { color: 'rgba(255,255,255,0.7)' }, content: { color: '#fff', fontWeight: 600 } }}>
                  <Descriptions.Item label="工号">{detailData?.employeeNo || '--'}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col xs={24} md={8}>
                <Descriptions column={1} size="small" styles={{ label: { color: 'rgba(255,255,255,0.7)' }, content: { color: '#fff', fontWeight: 600 } }}>
                  <Descriptions.Item label="部门">{detailData?.deptName || '--'}</Descriptions.Item>
                </Descriptions>
              </Col>
            </Row>
          </div>

          <Row gutter={[16, 16]}>
            <Col xs={24} md={12}>
              <Card bordered={false} style={{ borderRadius: 18, height: '100%', background: '#fafafa' }}>
                <Space direction="vertical" size={18} style={{ width: '100%' }}>
                  <Title level={4} style={{ margin: 0, color: '#16a34a' }}>
                    收入明细
                  </Title>
                  <Space direction="vertical" size={14} style={{ width: '100%' }}>
                    {incomeItems.map((item) => (
                      <Row key={item.label} justify="space-between">
                        <Text>{item.label}</Text>
                        <Text strong>{formatCurrency(item.amount)}</Text>
                      </Row>
                    ))}
                    <Card
                      size="small"
                      bordered={false}
                      style={{ background: '#edfdf3', borderRadius: 12 }}
                    >
                      <Row justify="space-between">
                        <Text strong style={{ color: '#15803d' }}>
                          应发小计
                        </Text>
                        <Text strong style={{ color: '#15803d', fontSize: 18 }}>
                          {formatCurrency(detailData?.grossSalary)}
                        </Text>
                      </Row>
                    </Card>
                  </Space>
                </Space>
              </Card>
            </Col>
            <Col xs={24} md={12}>
              <Card bordered={false} style={{ borderRadius: 18, height: '100%', background: '#fafafa' }}>
                <Space direction="vertical" size={18} style={{ width: '100%' }}>
                  <Title level={4} style={{ margin: 0, color: '#ef4444' }}>
                    扣除明细
                  </Title>
                  <Space direction="vertical" size={14} style={{ width: '100%' }}>
                    {deductionItems.map((item) => (
                      <Row key={item.label} justify="space-between">
                        <Text>{item.label}</Text>
                        <Text strong type="danger">
                          -{formatCurrency(item.amount)}
                        </Text>
                      </Row>
                    ))}
                    <Card
                      size="small"
                      bordered={false}
                      style={{ background: '#fff1f2', borderRadius: 12 }}
                    >
                      <Row justify="space-between">
                        <Text strong style={{ color: '#dc2626' }}>
                          应扣小计
                        </Text>
                        <Text strong style={{ color: '#dc2626', fontSize: 18 }}>
                          -{formatCurrency(detailData?.deductionTotal)}
                        </Text>
                      </Row>
                    </Card>
                  </Space>
                </Space>
              </Card>
            </Col>
          </Row>

          <Card
            bordered={false}
            style={{
              marginTop: 20,
              borderRadius: 18,
              background: '#eef4ff',
              textAlign: 'center',
            }}
          >
            <Space direction="vertical" size={8} style={{ width: '100%' }}>
              <Text type="secondary">实发工资</Text>
              <Title level={1} style={{ margin: 0, color: '#2563eb' }}>
                {formatCurrency(detailData?.netSalary)}
              </Title>
              <Text type="secondary">
                税后实发金额，已扣除个人社保、公积金及个税
              </Text>
            </Space>
          </Card>
        </Spin>
      </Modal>
    </PageContainer>
  );
};

export default SalaryPayslipManagePage;

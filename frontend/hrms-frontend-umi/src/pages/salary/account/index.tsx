import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type { EmployeeBrief } from '@/services/employee';
import { getEmployeeList } from '@/services/employee';
import type { DepartmentTree, DictData } from '@/services/organization';
import { getDepartmentTree, getDictDataByType } from '@/services/organization';
import type {
  SalaryTemplate,
  SalaryTemplateCreateOrUpdateRequest,
  SalaryTemplateItem,
  SalaryTemplateQuery,
} from '@/services/salary';
import {
  createSalaryTemplate,
  getSalaryTemplateList,
  updateSalaryTemplate,
} from '@/services/salary';
import {
  EditOutlined,
  PlusOutlined,
  PoweroffOutlined,
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
  Drawer,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Radio,
  Row,
  Select,
  Space,
  Table,
  Tag,
  TreeSelect,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Text, Title } = Typography;
const { TextArea } = Input;

const SALARY_TEMPLATE_STORAGE_PREFIX = 'salary-template-query';
const DEFAULT_PAGE_SIZE = 10;
const JOB_LEVEL_DICT_TYPE = 'job_level';

const SCOPE_TYPE_OPTIONS = [
  { label: '全部', value: 'ALL' },
  { label: '部门', value: 'DEPT' },
  { label: '员工', value: 'EMPLOYEE' },
  { label: '职级', value: 'JOB_LEVEL' },
];

const SCOPE_FILTER_OPTIONS = [
  { label: '部门', value: 'DEPT' },
  { label: '员工', value: 'EMPLOYEE' },
  { label: '职级', value: 'JOB_LEVEL' },
];

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const ITEM_CATEGORY_OPTIONS = [
  { label: '固定收入', value: 'FIXED_INCOME' },
  { label: '变动收入', value: 'VARIABLE_INCOME' },
  { label: '社保', value: 'SOCIAL_INSURANCE' },
  { label: '公积金', value: 'HOUSING_FUND' },
  { label: '个税', value: 'INCOME_TAX' },
];

const CATEGORY_META: Record<string, { label: string; color: string }> = {
  FIXED_INCOME: { label: '固定收入', color: 'blue' },
  FIXED: { label: '固定收入', color: 'blue' },
  VARIABLE_INCOME: { label: '变动收入', color: 'gold' },
  VARIABLE: { label: '变动收入', color: 'gold' },
  SOCIAL_INSURANCE: { label: '社保', color: 'green' },
  HOUSING_FUND: { label: '公积金', color: 'cyan' },
  INCOME_TAX: { label: '个税', color: 'volcano' },
  TAX: { label: '个税', color: 'volcano' },
};

interface SalaryTemplateFilterValues {
  templateName?: string;
  scope?: string;
  status?: number;
}

interface SalaryTemplateQueryState {
  templateName?: string;
  scope?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}

interface SalaryTemplateFormValues {
  templateName: string;
  effectiveDate: Dayjs;
  status?: number;
  remark?: string;
  scopeType?: string;
  scopeSelection?: Array<string | number> | string;
  items?: SalaryTemplateItem[];
}

interface StoredSalaryTemplateQueryState extends Partial<SalaryTemplateQueryState> {}

/** 生成账套查询缓存键，按用户隔离最近一次筛选条件。 */
function resolveStorageKey() {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) {
    return `${SALARY_TEMPLATE_STORAGE_PREFIX}:anonymous`;
  }

  try {
    const userInfo = JSON.parse(userInfoText) as {
      userId?: number | string;
      id?: number | string;
      username?: string;
    };
    const identity = userInfo.userId || userInfo.id || userInfo.username || 'anonymous';
    return `${SALARY_TEMPLATE_STORAGE_PREFIX}:${identity}`;
  } catch {
    return `${SALARY_TEMPLATE_STORAGE_PREFIX}:anonymous`;
  }
}

/** 读取上次账套查询条件，用于页面回显和恢复列表状态。 */
function getStoredQuery(): StoredSalaryTemplateQueryState {
  const storedText = sessionStorage.getItem(resolveStorageKey());
  if (!storedText) {
    return {};
  }

  try {
    return JSON.parse(storedText) as StoredSalaryTemplateQueryState;
  } catch {
    sessionStorage.removeItem(resolveStorageKey());
    return {};
  }
}

/** 格式化账套生效日期。 */
function formatDate(value?: string) {
  if (!value) {
    return '--';
  }
  const date = dayjs(value);
  return date.isValid() ? date.format('YYYY-MM-DD') : value;
}

/** 渲染账套启停状态标签。 */
function renderStatusTag(status?: number) {
  if (status === 1) {
    return <Tag color="success">启用</Tag>;
  }
  return <Tag color="default">停用</Tag>;
}

/** 渲染薪资项目分类标签。 */
function renderCategoryTag(category?: string) {
  if (!category) {
    return <Tag>--</Tag>;
  }
  const meta = CATEGORY_META[category];
  return <Tag color={meta?.color || 'default'}>{meta?.label || category}</Tag>;
}

/** 构造账套列表查询参数。 */
function buildTemplateQuery(query: SalaryTemplateQueryState): SalaryTemplateQuery {
  return {
    templateName: query.templateName,
    scope: query.scope,
    status: query.status,
    pageNum: query.pageNum,
    pageSize: query.pageSize,
  };
}

/** 生成账套适用范围值，供表单提交时转换不同范围类型。 */
function buildScopeValue(
  scopeType: string | undefined,
  scopeSelection?: Array<string | number> | string,
) {
  if (!scopeType || scopeType === 'ALL') {
    return undefined;
  }
  if (scopeType === 'DEPT' || scopeType === 'JOB_LEVEL') {
    if (Array.isArray(scopeSelection)) {
      return scopeSelection.length > 0 ? scopeSelection.join(',') : undefined;
    }
    return scopeSelection ? String(scopeSelection) : undefined;
  }
  if (typeof scopeSelection === 'string') {
    return scopeSelection.trim() || undefined;
  }
  if (Array.isArray(scopeSelection)) {
    return scopeSelection.join(',') || undefined;
  }
  return undefined;
}

/** 解析账套适用范围值，供编辑账套时回填范围控件。 */
function parseScopeSelection(scopeType?: string, scopeValue?: string) {
  if (!scopeValue) {
    return undefined;
  }
  if (scopeType === 'DEPT' || scopeType === 'JOB_LEVEL') {
    return scopeValue.split(',').map((item) => item.trim()).filter(Boolean);
  }
  return scopeValue;
}

/** 构造账套提交载荷，内部调用 `buildScopeValue` 统一范围字段格式。 */
function buildTemplatePayload(values: SalaryTemplateFormValues): SalaryTemplateCreateOrUpdateRequest {
  return {
    templateName: values.templateName.trim(),
    scopeType: values.scopeType || 'ALL',
    scopeValue: buildScopeValue(values.scopeType, values.scopeSelection),
    effectiveDate: values.effectiveDate?.format('YYYY-MM-DD'),
    status: values.status ?? 1,
    remark: values.remark?.trim() || undefined,
    items: (values.items || []).map((item, index) => ({
      itemCode: item.itemCode.trim(),
      itemName: item.itemName.trim(),
      category: item.category,
      calcRule: item.calcRule?.trim() || undefined,
      defaultValue: item.defaultValue,
      sortNo: item.sortNo ?? index + 1,
    })),
  };
}

/** 构造账套启停切换载荷，保留原账套配置并只切换状态。 */
function buildTogglePayload(template: SalaryTemplate): SalaryTemplateCreateOrUpdateRequest {
  return {
    templateName: template.templateName,
    templateCode: template.templateCode,
    scopeType: template.scopeType,
    scopeValue: template.scopeValue,
    effectiveDate: template.effectiveDate,
    status: template.status === 1 ? 0 : 1,
    remark: template.remark,
    items: (template.items || []).map((item) => ({
      itemCode: item.itemCode,
      itemName: item.itemName,
      category: item.category,
      calcRule: item.calcRule,
      defaultValue: item.defaultValue,
      sortNo: item.sortNo,
    })),
  };
}

/** 规范化员工选项列表，供账套适用范围按员工选择时复用。 */
function normalizeEmployeeOptions(records?: EmployeeBrief[]) {
  return (records || []).map((item) => ({
    label: `${item.employeeName} (${item.employeeNo})`,
    value: String(item.id),
  }));
}

/** 将部门树转换为 TreeSelect 组件数据结构。 */
function toTreeSelectData(nodes?: DepartmentTree[]): Array<{
  title: string;
  value: string;
  key: string;
  children?: any[];
}> {
  if (!nodes) {
    return [];
  }
  return nodes.map((node) => ({
    title: node.deptName,
    value: String(node.id),
    key: String(node.id),
    children: toTreeSelectData(node.children),
  }));
}

/**
 * 薪资账套页面组件。
 * 负责账套分页查询、适用范围配置、新增编辑和启停切换。
 */
const SalaryAccountPage: React.FC = () => {
  const [form] = Form.useForm<SalaryTemplateFilterValues>();
  const [drawerForm] = Form.useForm<SalaryTemplateFormValues>();
  const storedQuery = getStoredQuery();
  const [query, setQuery] = useState<SalaryTemplateQueryState>({
    templateName: storedQuery.templateName,
    scope: storedQuery.scope,
    status: storedQuery.status,
    pageNum: storedQuery.pageNum || 1,
    pageSize: storedQuery.pageSize || DEFAULT_PAGE_SIZE,
  });
  const [templatePageData, setTemplatePageData] = useState<PageResult<SalaryTemplate>>();
  const [tableLoading, setTableLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<SalaryTemplate>();
  const [departmentTree, setDepartmentTree] = useState<DepartmentTree[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [jobLevelOptions, setJobLevelOptions] = useState<DictData[]>([]);
  const [jobLevelLoading, setJobLevelLoading] = useState(false);
  const [employeeOptions, setEmployeeOptions] = useState<Array<{ label: string; value: string }>>([]);
  const [employeeLoading, setEmployeeLoading] = useState(false);
  const currentScopeType = Form.useWatch('scopeType', drawerForm) || 'ALL';

  /** 加载账套列表，内部调用 `buildTemplateQuery` 统一请求参数。 */
  const loadTemplates = async (nextQuery: SalaryTemplateQueryState) => {
    setTableLoading(true);
    try {
      const nextPageData = await getSalaryTemplateList(buildTemplateQuery(nextQuery));
      setTemplatePageData(nextPageData);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '薪资账套加载失败';
      message.error(messageText);
    } finally {
      setTableLoading(false);
    }
  };

  /** 加载部门树，供账套按部门适用范围选择使用。 */
  const loadDepartmentTree = async () => {
    setDepartmentLoading(true);
    try {
      const nextTree = await getDepartmentTree();
      setDepartmentTree(nextTree || []);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '部门树加载失败';
      message.error(messageText);
    } finally {
      setDepartmentLoading(false);
    }
  };

  /** 加载职级字典，供账套按职级适用范围选择使用。 */
  const loadJobLevelOptions = async () => {
    setJobLevelLoading(true);
    try {
      const nextOptions = await getDictDataByType(JOB_LEVEL_DICT_TYPE);
      setJobLevelOptions(nextOptions || []);
    } catch (error) {
      const fallback: DictData[] = ['P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7', 'P8', 'M1', 'M2', 'M3'].map(
        (value, index) => ({
          id: index + 1,
          dictType: JOB_LEVEL_DICT_TYPE,
          dictLabel: value,
          dictValue: value,
          label: value,
          value,
          sort: index + 1,
          status: 1,
        }),
      );
      setJobLevelOptions(fallback);
      const messageText = error instanceof Error ? error.message : '职级选项加载失败，已使用默认选项';
      message.warning(messageText);
    } finally {
      setJobLevelLoading(false);
    }
  };

  /** 加载员工列表，供账套按员工适用范围选择使用。 */
  const loadEmployeeOptions = async () => {
    setEmployeeLoading(true);
    try {
      const page = await getEmployeeList({ pageNum: 1, pageSize: 200 });
      setEmployeeOptions(normalizeEmployeeOptions(page.records));
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '员工列表加载失败';
      message.error(messageText);
    } finally {
      setEmployeeLoading(false);
    }
  };

  useEffect(() => {
    void loadDepartmentTree();
    void loadJobLevelOptions();
    void loadEmployeeOptions();
  }, []);

  useEffect(() => {
    form.setFieldsValue({
      templateName: query.templateName,
      scope: query.scope,
      status: query.status,
    });
  }, [form, query.scope, query.status, query.templateName]);

  useEffect(() => {
    void loadTemplates(query);
  }, [query]);

  useEffect(() => {
    sessionStorage.setItem(resolveStorageKey(), JSON.stringify(query));
  }, [query]);

  usePageAutoRefresh(() => {
    void loadTemplates(query);
    void loadDepartmentTree();
    void loadJobLevelOptions();
  });

  /** 打开新建账套抽屉，并重置表单与编辑上下文。 */
  const openCreateDrawer = () => {
    setEditingTemplate(undefined);
    drawerForm.resetFields();
    drawerForm.setFieldsValue({
      status: 1,
      scopeType: 'ALL',
      items: [
        {
          itemCode: '',
          itemName: '',
          category: 'FIXED_INCOME',
          sortNo: 1,
        },
      ],
    });
    setDrawerOpen(true);
  };

  /** 打开编辑账套抽屉，内部调用范围解析方法回填适用范围。 */
  const openEditDrawer = (template: SalaryTemplate) => {
    setEditingTemplate(template);
    drawerForm.resetFields();
    drawerForm.setFieldsValue({
      templateName: template.templateName,
      effectiveDate: template.effectiveDate ? dayjs(template.effectiveDate) : undefined,
      status: template.status ?? 1,
      remark: template.remark,
      scopeType: template.scopeType || 'ALL',
      scopeSelection: parseScopeSelection(template.scopeType, template.scopeValue),
      items:
        template.items?.map((item) => ({
          itemCode: item.itemCode,
          itemName: item.itemName,
          category: item.category,
          calcRule: item.calcRule,
          defaultValue: item.defaultValue == null ? undefined : Number(item.defaultValue),
          sortNo: item.sortNo,
        })) || [],
    });
    setDrawerOpen(true);
  };

  const closeDrawer = () => {
    setDrawerOpen(false);
    setEditingTemplate(undefined);
    drawerForm.resetFields();
  };

  /** 执行账套查询并重置分页。 */
  const handleSearch = (values: SalaryTemplateFilterValues) => {
    setQuery((previous) => ({
      ...previous,
      templateName: values.templateName?.trim() || undefined,
      scope: values.scope,
      status: values.status,
      pageNum: 1,
    }));
  };

  /** 重置账套筛选条件并恢复默认分页。 */
  const handleReset = () => {
    setQuery({
      pageNum: 1,
      pageSize: query.pageSize,
    });
  };

  /** 提交账套表单，内部调用 `buildTemplatePayload` 组装保存参数并刷新列表。 */
  const handleSubmit = async () => {
    try {
      const values = await drawerForm.validateFields();
      const payload = buildTemplatePayload(values);
      setSubmitting(true);
      if (editingTemplate) {
        await updateSalaryTemplate(editingTemplate.id, payload);
        message.success('薪资账套已更新');
      } else {
        await createSalaryTemplate(payload);
        message.success('薪资账套已创建');
      }
      closeDrawer();
      await loadTemplates(query);
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  /** 切换账套启停状态，内部调用 `buildTogglePayload` 复用原账套配置。 */
  const handleToggleStatus = async (template: SalaryTemplate) => {
    try {
      await updateSalaryTemplate(template.id, buildTogglePayload(template));
      message.success(template.status === 1 ? '账套已停用' : '账套已启用');
      await loadTemplates(query);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : '账套状态切换失败';
      message.error(messageText);
    }
  };

  const columns: ColumnsType<SalaryTemplate> = [
    {
      title: '账套名称',
      dataIndex: 'templateName',
      width: 220,
      render: (value) => <Text strong>{value || '--'}</Text>,
    },
    {
      title: '适用范围',
      dataIndex: 'scopeName',
      width: 180,
      render: (value) => value || '--',
    },
    {
      title: '生效日期',
      dataIndex: 'effectiveDate',
      width: 140,
      render: (value) => formatDate(value),
    },
    {
      title: '工资项目数量',
      dataIndex: 'itemCount',
      width: 120,
      render: (value) => (value == null ? '--' : `${value} 项`),
    },
    {
      title: '工资项目',
      dataIndex: 'items',
      width: 260,
      render: (items: SalaryTemplateItem[] | undefined) =>
        items && items.length > 0 ? (
          <Space wrap size={[4, 6]}>
            {Array.from(new Set(items.map((item) => item.category))).map((category) => (
              <React.Fragment key={category}>{renderCategoryTag(category)}</React.Fragment>
            ))}
          </Space>
        ) : (
          <Text type="secondary">--</Text>
        ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value) => renderStatusTag(value),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right',
      render: (_value, record) => (
        <Space size={4}>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEditDrawer(record)}>
            编辑
          </Button>
          <Popconfirm
            title={record.status === 1 ? '确认停用该账套？' : '确认启用该账套？'}
            onConfirm={() => void handleToggleStatus(record)}
          >
            <Button type="link" size="small" icon={<PoweroffOutlined />}>
              {record.status === 1 ? '停用' : '启用'}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const departmentTreeData = useMemo(() => toTreeSelectData(departmentTree), [departmentTree]);
  const jobLevelSelectOptions = useMemo(
    () =>
      jobLevelOptions.map((item) => ({
        label: item.dictLabel || item.label || item.dictValue || item.value || '--',
        value: item.dictValue || item.value || item.dictLabel || item.label || '',
      })),
    [jobLevelOptions],
  );

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            薪资账套
          </Title>
          <Text type="secondary">维护不同组织范围的薪资模板与工资项目，为薪资核算和员工薪资档案提供统一规则。</Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          initialValues={{
            templateName: query.templateName,
            scope: query.scope,
            status: query.status,
          }}
          style={{ rowGap: 16 }}
        >
          <Form.Item label="账套名称" name="templateName">
            <Input allowClear placeholder="请输入账套名称" style={{ width: 220 }} />
          </Form.Item>
          <Form.Item label="适用范围" name="scope">
            <Select
              allowClear
              placeholder="请选择适用范围"
              options={SCOPE_FILTER_OPTIONS}
              style={{ width: 180 }}
            />
          </Form.Item>
          <Form.Item label="状态" name="status">
            <Select
              allowClear
              placeholder="请选择状态"
              options={STATUS_OPTIONS}
              style={{ width: 160 }}
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
        title="账套列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateDrawer}>
            新增账套
          </Button>
        }
      >
        <Table<SalaryTemplate>
          rowKey="id"
          columns={columns}
          dataSource={templatePageData?.records || []}
          loading={tableLoading}
          scroll={{ x: 1220 }}
          pagination={{
            current: templatePageData?.pageNum || query.pageNum,
            pageSize: templatePageData?.pageSize || query.pageSize,
            total: templatePageData?.total || 0,
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

      <Drawer
        title={editingTemplate ? '编辑账套' : '新增账套'}
        width={720}
        open={drawerOpen}
        onClose={closeDrawer}
        destroyOnClose
        extra={
          <Space>
            <Button onClick={closeDrawer}>取消</Button>
            <Button type="primary" loading={submitting} onClick={() => void handleSubmit()}>
              保存
            </Button>
          </Space>
        }
      >
        <Form
          form={drawerForm}
          layout="vertical"
          initialValues={{
            status: 1,
            scopeType: 'ALL',
            items: [],
          }}
        >
          <Card bordered={false} style={{ marginBottom: 16, background: '#fafafa' }}>
            <Title level={5}>基本信息</Title>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="账套名称"
                  name="templateName"
                  rules={[{ required: true, message: '请输入账套名称' }]}
                >
                  <Input placeholder="请输入账套名称" maxLength={50} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="生效日期"
                  name="effectiveDate"
                  rules={[{ required: true, message: '请选择生效日期' }]}
                >
                  <DatePicker style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="状态" name="status">
                  <Radio.Group options={STATUS_OPTIONS} optionType="button" />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="备注" name="remark">
                  <TextArea rows={3} maxLength={200} showCount placeholder="请输入备注（选填）" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          <Card bordered={false} style={{ marginBottom: 16, background: '#fafafa' }}>
            <Title level={5}>适用范围</Title>
            <Form.Item label="适用范围类型" name="scopeType">
              <Radio.Group options={SCOPE_TYPE_OPTIONS} optionType="button" />
            </Form.Item>

            {currentScopeType === 'DEPT' ? (
              <Form.Item
                label="选择部门"
                name="scopeSelection"
                rules={[{ required: true, message: '请选择适用部门' }]}
              >
                <TreeSelect
                  treeData={departmentTreeData}
                  treeCheckable
                  showCheckedStrategy={TreeSelect.SHOW_CHILD}
                  placeholder="请选择适用部门"
                  loading={departmentLoading}
                  allowClear
                  multiple
                  style={{ width: '100%' }}
                />
              </Form.Item>
            ) : null}

            {currentScopeType === 'EMPLOYEE' ? (
              <Form.Item
                label="选择员工"
                name="scopeSelection"
                rules={[{ required: true, message: '请选择适用员工' }]}
              >
                <Select
                  mode="multiple"
                  showSearch
                  allowClear
                  loading={employeeLoading}
                  options={employeeOptions}
                  placeholder="请选择适用员工"
                  optionFilterProp="label"
                />
              </Form.Item>
            ) : null}

            {currentScopeType === 'JOB_LEVEL' ? (
              <Form.Item
                label="选择职级"
                name="scopeSelection"
                rules={[{ required: true, message: '请选择适用职级' }]}
              >
                <Select
                  mode="multiple"
                  showSearch
                  allowClear
                  loading={jobLevelLoading}
                  options={jobLevelSelectOptions}
                  placeholder="请选择适用职级"
                  optionFilterProp="label"
                />
              </Form.Item>
            ) : null}
          </Card>

          <Card bordered={false} style={{ background: '#fafafa' }}>
            <Space
              style={{ width: '100%', justifyContent: 'space-between', marginBottom: 16 }}
              align="center"
            >
              <Title level={5} style={{ margin: 0 }}>
                工资项目
              </Title>
            </Space>

            <Form.List name="items">
              {(fields, { add, remove }) => (
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {fields.map((field, index) => (
                    <Card
                      key={field.key}
                      size="small"
                      title={`项目 ${index + 1}`}
                      extra={
                        <Button danger type="link" onClick={() => remove(field.name)}>
                          删除
                        </Button>
                      }
                    >
                      <Row gutter={12}>
                        <Col span={8}>
                          <Form.Item
                            {...field}
                            label="项目编码"
                            name={[field.name, 'itemCode']}
                            rules={[{ required: true, message: '请输入项目编码' }]}
                          >
                            <Input placeholder="如 BASIC_PAY" />
                          </Form.Item>
                        </Col>
                        <Col span={8}>
                          <Form.Item
                            {...field}
                            label="项目名称"
                            name={[field.name, 'itemName']}
                            rules={[{ required: true, message: '请输入项目名称' }]}
                          >
                            <Input placeholder="如 基本工资" />
                          </Form.Item>
                        </Col>
                        <Col span={8}>
                          <Form.Item
                            {...field}
                            label="项目分类"
                            name={[field.name, 'category']}
                            rules={[{ required: true, message: '请选择项目分类' }]}
                          >
                            <Select options={ITEM_CATEGORY_OPTIONS} placeholder="请选择分类" />
                          </Form.Item>
                        </Col>
                        <Col span={10}>
                          <Form.Item {...field} label="计算规则" name={[field.name, 'calcRule']}>
                            <Input placeholder="如 固定值 / 按公式计算" />
                          </Form.Item>
                        </Col>
                        <Col span={7}>
                          <Form.Item {...field} label="默认值" name={[field.name, 'defaultValue']}>
                            <InputNumber
                              min={0}
                              precision={2}
                              style={{ width: '100%' }}
                              placeholder="请输入默认值"
                            />
                          </Form.Item>
                        </Col>
                        <Col span={7}>
                          <Form.Item {...field} label="排序号" name={[field.name, 'sortNo']}>
                            <InputNumber
                              min={1}
                              precision={0}
                              style={{ width: '100%' }}
                              placeholder="请输入排序号"
                            />
                          </Form.Item>
                        </Col>
                      </Row>
                    </Card>
                  ))}

                  <Button
                    type="dashed"
                    icon={<PlusOutlined />}
                    onClick={() =>
                      add({
                        itemCode: '',
                        itemName: '',
                        category: 'FIXED_INCOME',
                        sortNo: fields.length + 1,
                      })
                    }
                    block
                  >
                    添加工资项目
                  </Button>
                </Space>
              )}
            </Form.List>
          </Card>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default SalaryAccountPage;

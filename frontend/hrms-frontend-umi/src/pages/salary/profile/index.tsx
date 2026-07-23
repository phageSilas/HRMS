import { getEmployeeDetail, getEmployeeList, type EmployeeBrief } from '@/services/employee';
import { getDeptList, type DeptListItem } from '@/services/organization';
import {
  getSalaryEmployeeProfileDetail,
  getSalaryTemplateList,
  updateSalaryEmployeeProfile,
  type SalaryEmployeeProfileDetail,
  type SalaryEmployeeProfileHistoryItem,
  type SalaryEmployeeProfileUpdateRequest,
  type SalaryTemplate,
} from '@/services/salary';
import { EditOutlined } from '@ant-design/icons';
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
  InputNumber,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { type Dayjs } from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';

const { Title, Text } = Typography;
const { TextArea } = Input;

type SearchFormValues = {
  employeeId?: number;
  deptId?: number;
  employeeOptionId?: number;
  employeeNoOptionId?: number;
};

type ProfileEditFormValues = {
  templateId: number;
  baseSalary: number;
  allowance?: number;
  performanceBase?: number;
  socialInsuranceBase?: number;
  housingFundBase?: number;
  probationSalaryRatio?: number;
  effectiveDate?: Dayjs;
  remark?: string;
  changeReason: string;
};

type EmployeeOption = {
  label: string;
  value: number;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
};

/**
 * 格式化金额显示。
 *
 * @param value 金额值
 * @returns 格式化后的金额文本
 */
function formatMoney(value?: number | string | null) {
  if (value == null || value === '') {
    return '--';
  }
  const amount = Number(value);
  if (!Number.isFinite(amount)) {
    return String(value);
  }
  return `￥${amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

/**
 * 渲染金额前后差异文本。
 *
 * @param before 调整前金额
 * @param after 调整后金额
 * @returns 差异文本
 */
function renderDiff(before?: number | string, after?: number | string) {
  const beforeText = formatMoney(before);
  const afterText = formatMoney(after);
  if (beforeText === afterText) {
    return afterText;
  }
  return `${beforeText} -> ${afterText}`;
}

/**
 * 渲染普通文本前后差异。
 *
 * @param before 调整前文本
 * @param after 调整后文本
 * @returns 差异文本
 */
function renderTextDiff(before?: string | number, after?: string | number) {
  const beforeText = before == null || before === '' ? '--' : String(before);
  const afterText = after == null || after === '' ? '--' : String(after);
  if (beforeText === afterText) {
    return afterText;
  }
  return `${beforeText} -> ${afterText}`;
}

/**
 * 薪资档案页面。
 *
 * 负责员工薪资档案查询、详情展示和档案维护。
 *
 * @returns 页面组件
 */
const SalaryProfilePage: React.FC = () => {
  const [searchForm] = Form.useForm<SearchFormValues>();
  const [editForm] = Form.useForm<ProfileEditFormValues>();
  const [departments, setDepartments] = useState<DeptListItem[]>([]);
  const [templates, setTemplates] = useState<SalaryTemplate[]>([]);
  const [employeeNameOptions, setEmployeeNameOptions] = useState<EmployeeOption[]>([]);
  const [employeeNoOptions, setEmployeeNoOptions] = useState<EmployeeOption[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [employeeNameLoading, setEmployeeNameLoading] = useState(false);
  const [employeeNoLoading, setEmployeeNoLoading] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [templateLoading, setTemplateLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number>();
  const [profileDetail, setProfileDetail] = useState<SalaryEmployeeProfileDetail>();
  const [templateOutOfScope, setTemplateOutOfScope] = useState(false);

  /**
   * 构建姓名搜索候选项。
   *
   * @param employee 员工简要信息
   * @returns 候选项
   */
  const buildEmployeeNameOption = (
    employee: Pick<EmployeeBrief, 'id' | 'employeeName' | 'employeeNo' | 'deptName'>,
  ): EmployeeOption => ({
    label: `${employee.employeeName}（${employee.employeeNo}）`,
    value: employee.id,
    employeeName: employee.employeeName,
    employeeNo: employee.employeeNo,
    deptName: employee.deptName,
  });

  /**
   * 构建工号搜索候选项。
   *
   * @param employee 员工简要信息
   * @returns 候选项
   */
  const buildEmployeeNoOption = (
    employee: Pick<EmployeeBrief, 'id' | 'employeeName' | 'employeeNo' | 'deptName'>,
  ): EmployeeOption => ({
    label: `${employee.employeeNo} - ${employee.employeeName}${employee.deptName ? `（${employee.deptName}）` : ''}`,
    value: employee.id,
    employeeName: employee.employeeName,
    employeeNo: employee.employeeNo,
    deptName: employee.deptName,
  });

  /**
   * 将当前员工同步到姓名和工号候选中，避免命中后下拉值丢失。
   *
   * @param employee 当前员工信息
   */
  const syncSelectedEmployeeOptions = (
    employee: Pick<EmployeeBrief, 'id' | 'employeeName' | 'employeeNo' | 'deptName'>,
  ) => {
    setEmployeeNameOptions([buildEmployeeNameOption(employee)]);
    setEmployeeNoOptions([buildEmployeeNoOption(employee)]);
  };

  /**
   * 清空当前员工上下文，避免残留旧详情。
   */
  const clearSelectedEmployeeContext = () => {
    setTemplateOutOfScope(false);
    setProfileDetail(undefined);
    setSelectedEmployeeId(undefined);
  };

  /**
   * 加载部门列表，供部门筛选使用。
   */
  const loadDepartments = async () => {
    setDepartmentLoading(true);
    try {
      const result = await getDeptList();
      setDepartments(result || []);
    } catch (error) {
      const text = error instanceof Error ? error.message : '部门列表加载失败';
      message.error(text);
    } finally {
      setDepartmentLoading(false);
    }
  };

  /**
   * 加载可用薪资账套，供编辑时选择。
   *
   * @param employeeId 员工ID
   * @returns 账套列表
   */
  const loadTemplates = async (employeeId?: number) => {
    setTemplateLoading(true);
    try {
      const result = await getSalaryTemplateList({
        pageNum: 1,
        pageSize: 200,
        status: 1,
        employeeId,
      });
      const nextTemplates = result.records || [];
      setTemplates(nextTemplates);
      return nextTemplates;
    } catch (error) {
      const text = error instanceof Error ? error.message : '薪资账套列表加载失败';
      setTemplates([]);
      message.error(text);
      return [];
    } finally {
      setTemplateLoading(false);
    }
  };

  useEffect(() => {
    void loadDepartments();
  }, []);

  const departmentOptions = useMemo(
    () =>
      departments.map((item) => ({
        label: item.deptName,
        value: item.id,
      })),
    [departments],
  );

  const templateOptions = useMemo(
    () =>
      templates.map((item) => ({
        label: item.templateName,
        value: item.id,
      })),
    [templates],
  );

  const isProbation = profileDetail?.employmentStatus === 1;

  /**
   * 加载薪资档案详情。
   *
   * @param employeeId 员工ID
   */
  const loadProfileDetail = async (employeeId: number) => {
    setProfileLoading(true);
    try {
      const detail = await getSalaryEmployeeProfileDetail(employeeId);
      setProfileDetail(detail);
      setSelectedEmployeeId(employeeId);
    } catch (error) {
      setProfileDetail(undefined);
      setSelectedEmployeeId(undefined);
      const text = error instanceof Error ? error.message : '薪资档案加载失败';
      message.error(text);
    } finally {
      setProfileLoading(false);
    }
  };

  /**
   * 按部门搜索姓名候选。
   *
   * @param deptId 部门ID
   * @param keyword 关键字
   */
  const searchEmployeeNameOptions = async (deptId?: number, keyword = '') => {
    if (!deptId) {
      setEmployeeNameOptions([]);
      return;
    }
    setEmployeeNameLoading(true);
    try {
      const page = await getEmployeeList({
        deptIds: [deptId],
        keyword: keyword.trim() || undefined,
        pageNum: 1,
        pageSize: 50,
      });
      setEmployeeNameOptions(
        (page.records || []).map((item: EmployeeBrief) => buildEmployeeNameOption(item)),
      );
    } catch (error) {
      setEmployeeNameOptions([]);
      const text = error instanceof Error ? error.message : '员工列表加载失败';
      message.error(text);
    } finally {
      setEmployeeNameLoading(false);
    }
  };

  /**
   * 按工号关键字搜索员工。
   *
   * @param keyword 工号关键字
   */
  const searchEmployeeNoOptions = async (keyword = '') => {
    const normalizedKeyword = keyword.trim();
    if (!normalizedKeyword) {
      if (profileDetail) {
        syncSelectedEmployeeOptions({
          id: profileDetail.employeeId,
          employeeName: profileDetail.employeeName || '',
          employeeNo: profileDetail.employeeNo || '',
          deptName: profileDetail.deptName || '',
        });
      } else {
        setEmployeeNoOptions([]);
      }
      return;
    }
    setEmployeeNoLoading(true);
    try {
      const page = await getEmployeeList({
        keyword: normalizedKeyword,
        pageNum: 1,
        pageSize: 50,
      });
      setEmployeeNoOptions(
        (page.records || []).map((item: EmployeeBrief) => buildEmployeeNoOption(item)),
      );
    } catch (error) {
      setEmployeeNoOptions([]);
      const text = error instanceof Error ? error.message : '员工工号列表加载失败';
      message.error(text);
    } finally {
      setEmployeeNoLoading(false);
    }
  };

  /**
   * 根据员工详情统一回填筛选项并刷新档案详情。
   *
   * @param detail 员工详情
   */
  const applyEmployeeSelection = async (detail: Awaited<ReturnType<typeof getEmployeeDetail>>) => {
    searchForm.setFieldsValue({
      employeeId: detail.id,
      deptId: detail.deptId,
      employeeOptionId: detail.id,
      employeeNoOptionId: detail.id,
    });
    syncSelectedEmployeeOptions({
      id: detail.id,
      employeeName: detail.employeeName,
      employeeNo: detail.employeeNo,
      deptName: detail.deptName,
    });
    await loadProfileDetail(detail.id);
  };

  /**
   * 按员工ID查询并回显。
   *
   * @param rawEmployeeId 员工ID原始值
   */
  const handleEmployeeIdLookup = async (rawEmployeeId?: number | string | null) => {
    const employeeId = Number(rawEmployeeId);
    if (!employeeId || employeeId < 1) {
      searchForm.setFieldsValue({
        employeeId: undefined,
        deptId: undefined,
        employeeOptionId: undefined,
        employeeNoOptionId: undefined,
      });
      setEmployeeNameOptions([]);
      setEmployeeNoOptions([]);
      clearSelectedEmployeeContext();
      return;
    }
    try {
      const detail = await getEmployeeDetail(employeeId);
      await applyEmployeeSelection(detail);
    } catch (error) {
      setEmployeeNameOptions([]);
      setEmployeeNoOptions([]);
      clearSelectedEmployeeContext();
      const text = error instanceof Error ? error.message : '员工信息加载失败';
      message.error(text);
    }
  };

  /**
   * 处理姓名候选选择。
   *
   * @param employeeId 员工ID
   */
  const handleEmployeeNameSelect = async (employeeId?: number) => {
    if (!employeeId) {
      const employeeNoOptionId = searchForm.getFieldValue('employeeNoOptionId');
      if (employeeNoOptionId) {
        searchForm.setFieldsValue({ employeeOptionId: undefined });
        return;
      }
      searchForm.setFieldsValue({
        employeeId: undefined,
        employeeOptionId: undefined,
        employeeNoOptionId: undefined,
      });
      setEmployeeNameOptions([]);
      setEmployeeNoOptions([]);
      clearSelectedEmployeeContext();
      return;
    }
    await handleEmployeeIdLookup(employeeId);
  };

  /**
   * 处理工号候选选择。
   *
   * @param employeeId 员工ID
   */
  const handleEmployeeNoSelect = async (employeeId?: number) => {
    if (!employeeId) {
      const employeeOptionId = searchForm.getFieldValue('employeeOptionId');
      if (employeeOptionId) {
        searchForm.setFieldsValue({ employeeNoOptionId: undefined });
        return;
      }
      searchForm.setFieldsValue({
        employeeId: undefined,
        deptId: undefined,
        employeeOptionId: undefined,
        employeeNoOptionId: undefined,
      });
      setEmployeeNameOptions([]);
      setEmployeeNoOptions([]);
      clearSelectedEmployeeContext();
      return;
    }
    try {
      const detail = await getEmployeeDetail(employeeId);
      await applyEmployeeSelection(detail);
    } catch (error) {
      setEmployeeNameOptions([]);
      setEmployeeNoOptions([]);
      clearSelectedEmployeeContext();
      const text = error instanceof Error ? error.message : '员工信息加载失败';
      message.error(text);
    }
  };

  /**
   * 打开薪资档案编辑弹窗并回填当前值。
   */
  const openEditModal = async () => {
    if (!profileDetail || !selectedEmployeeId) {
      return;
    }
    const availableTemplates = await loadTemplates(selectedEmployeeId);
    const currentTemplateAvailable =
      profileDetail.templateId == null
        ? true
        : availableTemplates.some((item) => item.id === profileDetail.templateId);
    setTemplateOutOfScope(!currentTemplateAvailable && profileDetail.templateId != null);
    editForm.setFieldsValue({
      templateId: currentTemplateAvailable ? profileDetail.templateId : undefined,
      baseSalary: Number(profileDetail.baseSalary || 0),
      allowance: Number(profileDetail.allowance || 0),
      performanceBase: Number(profileDetail.performanceBase || 0),
      socialInsuranceBase: Number(profileDetail.socialInsuranceBase || 0),
      housingFundBase: Number(profileDetail.housingFundBase || 0),
      probationSalaryRatio:
        profileDetail.probationSalaryRatio == null
          ? undefined
          : Number(profileDetail.probationSalaryRatio),
      effectiveDate: profileDetail.effectiveDate ? dayjs(profileDetail.effectiveDate) : undefined,
      remark: profileDetail.remark,
      changeReason: '',
    });
    if (!currentTemplateAvailable && profileDetail.templateId != null) {
      message.warning('当前已分配账套已不适用于该员工，请重新选择可用账套');
    }
    setEditOpen(true);
  };

  /**
   * 保存薪资档案调整。
   *
   * @param values 表单值
   * @returns 是否保存成功
   */
  const handleSave = async (values: ProfileEditFormValues) => {
    if (!selectedEmployeeId) {
      return false;
    }
    const payload: SalaryEmployeeProfileUpdateRequest = {
      templateId: values.templateId,
      baseSalary: values.baseSalary,
      allowance: values.allowance,
      performanceBase: values.performanceBase,
      socialInsuranceBase: values.socialInsuranceBase,
      housingFundBase: values.housingFundBase,
      probationSalaryRatio: isProbation ? values.probationSalaryRatio : undefined,
      effectiveDate: values.effectiveDate?.format('YYYY-MM-DD'),
      remark: values.remark?.trim() || undefined,
      changeReason: values.changeReason.trim(),
    };
    setSaving(true);
    try {
      await updateSalaryEmployeeProfile(selectedEmployeeId, payload);
      message.success('薪资档案已更新');
      setEditOpen(false);
      await loadProfileDetail(selectedEmployeeId);
      return true;
    } catch (error) {
      const text = error instanceof Error ? error.message : '薪资档案更新失败';
      message.error(text);
      return false;
    } finally {
      setSaving(false);
    }
  };

  const historyColumns: ColumnsType<SalaryEmployeeProfileHistoryItem> = [
    {
      title: '调整时间',
      dataIndex: 'createTime',
      width: 180,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '--'),
    },
    {
      title: '适用账套',
      width: 220,
      render: (_, record) => renderTextDiff(record.templateNameBefore, record.templateNameAfter),
    },
    {
      title: '基本工资',
      width: 180,
      render: (_, record) => renderDiff(record.baseSalaryBefore, record.baseSalaryAfter),
    },
    {
      title: '津贴基数',
      width: 180,
      render: (_, record) => renderDiff(record.allowanceBefore, record.allowanceAfter),
    },
    {
      title: '绩效基数',
      width: 180,
      render: (_, record) => renderDiff(record.performanceBaseBefore, record.performanceBaseAfter),
    },
    {
      title: '社保基数',
      width: 180,
      render: (_, record) =>
        renderDiff(record.socialInsuranceBaseBefore, record.socialInsuranceBaseAfter),
    },
    {
      title: '公积金基数',
      width: 180,
      render: (_, record) => renderDiff(record.housingFundBaseBefore, record.housingFundBaseAfter),
    },
    {
      title: '试用期薪资比例',
      width: 180,
      render: (_, record) =>
        renderTextDiff(record.probationSalaryRatioBefore, record.probationSalaryRatioAfter),
    },
    {
      title: '调整原因',
      dataIndex: 'changeReason',
      width: 220,
      render: (value) => value || '--',
    },
  ];

  return (
    <PageContainer
      title={false}
      content={
        <Space direction="vertical" size={4}>
          <Title level={2} style={{ margin: 0 }}>
            薪资档案
          </Title>
          <Text type="secondary">
            面向 HR 和财务查看并维护员工个人薪资档案，支持账套分配、基数调整和调薪历史追踪。
          </Text>
        </Space>
      }
    >
      <Card bordered={false} style={{ marginBottom: 20, borderRadius: 20 }}>
        <Form form={searchForm} layout="vertical">
          <Row gutter={[16, 16]} align="bottom">
            <Col xs={24} md={12} lg={6}>
              <Form.Item label="员工ID" name="employeeId">
                <Input
                  allowClear
                  placeholder="请输入员工ID"
                  onBlur={(event) => {
                    void handleEmployeeIdLookup(event.target.value);
                  }}
                  onPressEnter={(event) => {
                    event.preventDefault();
                    void handleEmployeeIdLookup((event.target as HTMLInputElement).value);
                  }}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12} lg={6}>
              <Form.Item label="部门" name="deptId">
                <Select
                  allowClear
                  showSearch
                  loading={departmentLoading}
                  optionFilterProp="label"
                  placeholder="请选择部门"
                  options={departmentOptions}
                  onChange={(value) => {
                    searchForm.setFieldsValue({
                      deptId: value,
                      employeeId: undefined,
                      employeeOptionId: undefined,
                      employeeNoOptionId: undefined,
                    });
                    setEmployeeNameOptions([]);
                    setEmployeeNoOptions([]);
                    clearSelectedEmployeeContext();
                    void searchEmployeeNameOptions(value);
                  }}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12} lg={6}>
              <Form.Item label="员工姓名" name="employeeOptionId">
                <Select
                  allowClear
                  showSearch
                  filterOption={false}
                  loading={employeeNameLoading}
                  disabled={!searchForm.getFieldValue('deptId')}
                  placeholder={searchForm.getFieldValue('deptId') ? '请输入姓名搜索员工' : '请先选择部门'}
                  options={employeeNameOptions}
                  onSearch={(value) => {
                    void searchEmployeeNameOptions(searchForm.getFieldValue('deptId'), value);
                  }}
                  onDropdownVisibleChange={(open) => {
                    if (open && searchForm.getFieldValue('deptId')) {
                      void searchEmployeeNameOptions(searchForm.getFieldValue('deptId'));
                    }
                  }}
                  onChange={(value) => {
                    void handleEmployeeNameSelect(value);
                  }}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12} lg={6}>
              <Form.Item label="员工工号" name="employeeNoOptionId">
                <Select
                  allowClear
                  showSearch
                  filterOption={false}
                  loading={employeeNoLoading}
                  placeholder="请输入工号搜索员工"
                  options={employeeNoOptions}
                  onSearch={(value) => {
                    void searchEmployeeNoOptions(value);
                  }}
                  onDropdownVisibleChange={(open) => {
                    if (open) {
                      void searchEmployeeNoOptions(profileDetail?.employeeNo || '');
                    }
                  }}
                  onChange={(value) => {
                    void handleEmployeeNoSelect(value);
                  }}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Card>

      <Spin spinning={profileLoading}>
        {!selectedEmployeeId || !profileDetail ? (
          <Card bordered={false} style={{ borderRadius: 20 }}>
            <Empty description="请选择员工查看薪资档案" />
          </Card>
        ) : (
          <Space direction="vertical" size={20} style={{ width: '100%' }}>
            {!profileDetail.assignedTemplate && (
              <Alert
                type="warning"
                showIcon
                message="当前员工尚未分配薪资账套"
                description="可通过右侧“编辑薪资档案”入口为该员工分配适用账套并维护基础薪资数据。"
              />
            )}

            <Card
              bordered={false}
              style={{ borderRadius: 20 }}
              extra={
                <Button type="primary" icon={<EditOutlined />} onClick={openEditModal}>
                  {profileDetail.assignedTemplate ? '编辑薪资档案' : '分配账套'}
                </Button>
              }
            >
              <Descriptions title="员工信息" column={3}>
                <Descriptions.Item label="员工ID">{profileDetail.employeeId}</Descriptions.Item>
                <Descriptions.Item label="员工姓名">{profileDetail.employeeName || '--'}</Descriptions.Item>
                <Descriptions.Item label="部门">{profileDetail.deptName || '--'}</Descriptions.Item>
                <Descriptions.Item label="工号">{profileDetail.employeeNo || '--'}</Descriptions.Item>
                <Descriptions.Item label="职位">{profileDetail.postName || '--'}</Descriptions.Item>
                <Descriptions.Item label="在职状态">
                  {profileDetail.employmentStatusDesc || profileDetail.employmentStatus || '--'}
                </Descriptions.Item>
              </Descriptions>

              <Descriptions title="薪资档案" column={2} style={{ marginTop: 24 }}>
                <Descriptions.Item label="适用账套">
                  {profileDetail.templateName || '--'}
                </Descriptions.Item>
                <Descriptions.Item label="基本工资">
                  {formatMoney(profileDetail.baseSalary)}
                </Descriptions.Item>
                <Descriptions.Item label="津贴基数">
                  {formatMoney(profileDetail.allowance)}
                </Descriptions.Item>
                <Descriptions.Item label="绩效基数">
                  {formatMoney(profileDetail.performanceBase)}
                </Descriptions.Item>
                <Descriptions.Item label="社保公积金基数">
                  {formatMoney(profileDetail.socialInsuranceBase)}
                </Descriptions.Item>
                <Descriptions.Item label="公积金基数">
                  {formatMoney(profileDetail.housingFundBase)}
                </Descriptions.Item>
                {isProbation && (
                  <Descriptions.Item label="试用期薪资信息" span={2}>
                    试用期薪资比例 {profileDetail.probationSalaryRatio || '--'}%，基本工资与津贴按试用期比例折算，社保公积金按全额基数缴纳。
                  </Descriptions.Item>
                )}
              </Descriptions>
            </Card>

            <Card bordered={false} style={{ borderRadius: 20 }} title="调薪历史">
              <Table<SalaryEmployeeProfileHistoryItem>
                rowKey="id"
                columns={historyColumns}
                dataSource={profileDetail.history || []}
                pagination={{ pageSize: 5 }}
                scroll={{ x: 1500 }}
              />
            </Card>
          </Space>
        )}
      </Spin>

      <Modal
        title={profileDetail?.assignedTemplate ? '编辑薪资档案' : '分配薪资账套'}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={() => {
          void editForm.submit();
        }}
        confirmLoading={saving}
        width={720}
        destroyOnClose
      >
        {templateOutOfScope && (
          <Alert
            type="warning"
            showIcon
            style={{ marginBottom: 16 }}
            message="当前已分配账套已不再适用于该员工，请重新选择可用账套"
          />
        )}
        <Form<ProfileEditFormValues> form={editForm} layout="vertical" onFinish={handleSave}>
          <Row gutter={[16, 16]}>
            <Col xs={24} md={12}>
              <Form.Item
                label="适用账套"
                name="templateId"
                rules={[{ required: true, message: '请选择适用账套' }]}
              >
                <Select
                  showSearch
                  loading={templateLoading}
                  optionFilterProp="label"
                  placeholder="请选择适用账套"
                  options={templateOptions}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                label="基本工资"
                name="baseSalary"
                rules={[{ required: true, message: '请输入基本工资' }]}
              >
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item label="津贴基数" name="allowance">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item label="绩效基数" name="performanceBase">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item label="社保公积金基数" name="socialInsuranceBase">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item label="公积金基数" name="housingFundBase">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            {isProbation && (
              <Col xs={24} md={12}>
                <Form.Item label="试用期薪资比例（%）" name="probationSalaryRatio">
                  <InputNumber min={0} precision={2} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            )}
            <Col xs={24} md={12}>
              <Form.Item label="生效日期" name="effectiveDate">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item
                label="调薪原因"
                name="changeReason"
                rules={[{ required: true, message: '请输入调薪原因' }]}
              >
                <TextArea rows={3} maxLength={200} showCount />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item label="备注" name="remark">
                <TextArea rows={3} maxLength={200} showCount />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default SalaryProfilePage;

/**
 * 调岗申请页面。对接调岗申请分页和创建接口。
 */

import {
  getEmployeeDetail,
  getEmployeeList,
  type Employee,
  type EmployeeBrief,
} from '@/services/employee';
import {
  getDeptDetail,
  getDeptList,
  getPostList,
  type PostItem,
} from '@/services/organization';
import type {
  TransferApplication,
  TransferApplicationCreateRequest,
} from '@/services/process';
import {
  ApprovalStatus,
  createTransferApplication,
  getTransferApplicationList,
  quickApproveTransferApplication,
} from '@/services/process';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  ModalForm,
  PageContainer,
  ProFormDatePicker,
  ProFormDigit,
  ProFormGroup,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { useSearchParams } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  Form,
  Popconfirm,
  Row,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { formatProcessDateTime } from '../utils';

const { Text } = Typography;
const SYSTEM_ADMIN_POST_ID = 3001;

type SelectOption = {
  label: string;
  value: number;
};

type JobLevelOption = {
  label: string;
  value: string;
};

const statusMeta: Record<number, { text: string; color: string }> = {
  [ApprovalStatus.DRAFT]: { text: '草稿', color: 'default' },
  [ApprovalStatus.APPROVING]: { text: '审批中', color: 'processing' },
  [ApprovalStatus.APPROVED]: { text: '已通过', color: 'success' },
  [ApprovalStatus.REJECTED]: { text: '已驳回', color: 'error' },
  [ApprovalStatus.WITHDRAWN]: { text: '已撤回', color: 'default' },
};

type TransferFormValues = TransferApplicationCreateRequest & {
  employeeName?: string;
  employeeNo?: string;
  fromDeptName?: string;
  fromPostName?: string;
  fromJobLevel?: string;
  fromLeaderId?: number;
};

/** 生成候选汇报人角色标签，区分 Leader、HR 或二者兼具。 */
function buildLeaderRoleLabel(
  employeeId: number,
  leaderIds: Set<number>,
  postName?: string,
) {
  const isLeader = leaderIds.has(employeeId);
  const isHr = /hr|人力/i.test(postName || '');
  if (isLeader && isHr) {
    return 'Leader / HR';
  }
  if (isLeader) {
    return 'Leader';
  }
  if (isHr) {
    return 'HR';
  }
  return '';
}

/** 根据岗位职级区间构造可选职级列表，供新岗位职级自动提示和校验。 */
function buildJobLevelOptions(post?: PostItem): JobLevelOption[] {
  const minLevel = post?.jobLevelMin?.trim();
  const maxLevel = post?.jobLevelMax?.trim();
  if (!minLevel && !maxLevel) {
    return [];
  }
  if (minLevel && !maxLevel) {
    return [{ label: minLevel, value: minLevel }];
  }
  if (!minLevel && maxLevel) {
    return [{ label: maxLevel, value: maxLevel }];
  }
  if (minLevel === maxLevel) {
    return [{ label: minLevel!, value: minLevel! }];
  }

  const minMatch = minLevel?.match(/^([A-Za-z]+)(\d+)$/);
  const maxMatch = maxLevel?.match(/^([A-Za-z]+)(\d+)$/);
  if (
    minMatch &&
    maxMatch &&
    minMatch[1] === maxMatch[1] &&
    Number(minMatch[2]) <= Number(maxMatch[2])
  ) {
    const levelPrefix = minMatch[1];
    const start = Number(minMatch[2]);
    const end = Number(maxMatch[2]);
    if (end - start <= 20) {
      return Array.from({ length: end - start + 1 }, (_, index) => {
        const level = `${levelPrefix}${start + index}`;
        return { label: level, value: level };
      });
    }
  }

  return Array.from(new Set([minLevel!, maxLevel!])).map((level) => ({
    label: level,
    value: level,
  }));
}

/**
 * 调岗申请页面组件。
 * 负责调岗申请列表查询、员工原岗位回填和目标岗位审批提交。
 */
const TransferPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [departmentOptions, setDepartmentOptions] = useState<SelectOption[]>(
    [],
  );
  const [realPostOptions, setRealPostOptions] = useState<PostItem[]>([]);
  const [realLeaderOptions, setRealLeaderOptions] = useState<SelectOption[]>(
    [],
  );
  const [employeeLoading, setEmployeeLoading] = useState(false);
  const [postLoading, setPostLoading] = useState(false);
  const [leaderLoading, setLeaderLoading] = useState(false);
  const [currentEmployeeDetail, setCurrentEmployeeDetail] =
    useState<Employee>();
  const [form] = Form.useForm<TransferFormValues>();

  const selectedToDeptId = Form.useWatch('toDeptId', form);
  const selectedToPostId = Form.useWatch('toPostId', form);
  const [searchParams] = useSearchParams();
  const employeeIdFromUrl = searchParams.get('employeeId');

  useEffect(() => {
    if (employeeIdFromUrl) {
      setModalOpen(true);
    }
  }, [employeeIdFromUrl]);

  useEffect(() => {
    const loadDepartments = async () => {
      try {
        const [departments, postPage] = await Promise.all([
          getDeptList(),
          getPostList({ pageNum: 1, pageSize: 200 }),
        ]);
        setDepartmentOptions(
          (departments || []).map((item) => ({
            label: item.deptName,
            value: item.id,
          })),
        );
        setRealPostOptions(
          (postPage.records || []).filter(
            (item) => item.id !== SYSTEM_ADMIN_POST_ID,
          ),
        );
      } catch (error) {
        message.error('部门或岗位数据加载失败，请刷新后重试');
      }
    };
    loadDepartments();
  }, []);

  const departmentFilterOption = useMemo(
    () => (input: string, option?: { label?: string | number }) =>
      String(option?.label || '')
        .toLowerCase()
        .includes(input.trim().toLowerCase()),
    [],
  );

  const postOptions = useMemo<SelectOption[]>(
    () =>
      (realPostOptions || []).map((item) => ({
        label: item.postName,
        value: item.id,
      })),
    [realPostOptions],
  );

  const selectedPost = useMemo(
    () => realPostOptions.find((item) => item.id === selectedToPostId),
    [realPostOptions, selectedToPostId],
  );

  const jobLevelOptions = useMemo(
    () => buildJobLevelOptions(selectedPost),
    [selectedPost],
  );

  const jobLevelPlaceholder = useMemo(() => {
    if (!selectedToPostId) {
      return '请先选择新职位';
    }
    if (!jobLevelOptions.length) {
      return '该岗位未配置职级，可手动输入';
    }
    if (jobLevelOptions.length === 1) {
      return '将自动带出职级';
    }
    return `可选: ${jobLevelOptions.map((item) => item.value).join(' / ')}`;
  }, [jobLevelOptions, selectedToPostId]);

  /** 重置员工关联信息，供员工未命中或关闭弹窗时统一清理表单状态。 */
  const resetEmployeeRelatedFields = () => {
    form.setFieldsValue({
      employeeName: undefined,
      employeeNo: undefined,
      fromDeptName: undefined,
      fromPostName: undefined,
      fromJobLevel: undefined,
      fromLeaderId: undefined,
    });
    setCurrentEmployeeDetail(undefined);
  };

  /** 加载员工基础信息，内部调用 `resetEmployeeRelatedFields` 处理无效员工场景。 */
  const handleEmployeeLookup = async (
    rawEmployeeId?: number | string | null,
  ) => {
    const employeeId = Number(rawEmployeeId);
    if (!employeeId || employeeId < 1) {
      resetEmployeeRelatedFields();
      return;
    }
    setEmployeeLoading(true);
    try {
      const detail = await getEmployeeDetail(employeeId);
      setCurrentEmployeeDetail(detail);
      form.setFieldsValue({
        employeeId: detail.id,
        employeeName: detail.employeeName,
        employeeNo: detail.employeeNo,
        fromDeptName: detail.deptName,
        fromPostName: detail.postName,
        fromJobLevel: detail.jobLevel,
        fromLeaderId: detail.leaderId,
      });
    } catch (error) {
      resetEmployeeRelatedFields();
      message.error('未找到该员工，请确认员工ID后重试');
    } finally {
      setEmployeeLoading(false);
    }
  };

  /** 触发员工信息查询，内部调用 `handleEmployeeLookup` 按表单员工 ID 回填原岗位信息。 */
  const triggerEmployeeLookup = () => {
    void handleEmployeeLookup(form.getFieldValue('employeeId'));
  };

  useEffect(() => {
    if (!modalOpen || !selectedToDeptId) {
      setRealLeaderOptions([]);
      setLeaderLoading(false);
      if (!selectedToDeptId) {
        form.setFieldsValue({
          toLeaderId: undefined,
        });
      }
      return;
    }

    let cancelled = false;

    const loadDeptRelatedOptions = async () => {
      setLeaderLoading(true);
      try {
        const currentDept = await getDeptDetail(selectedToDeptId);

        if (cancelled) {
          return;
        }

        const parentDept =
          currentDept.parentId && currentDept.parentId > 0
            ? await getDeptDetail(currentDept.parentId)
            : undefined;

        if (cancelled) {
          return;
        }

        const targetDeptIds = [
          currentDept.id,
          ...(parentDept?.id ? [parentDept.id] : []),
        ];
        const leaderIds = new Set<number>(
          [currentDept.leaderEmployeeId, parentDept?.leaderEmployeeId].filter(
            (item): item is number => typeof item === 'number' && item > 0,
          ),
        );
        const employeePage = await getEmployeeList({
          deptIds: targetDeptIds,
          pageNum: 1,
          pageSize: 200,
        });

        if (cancelled) {
          return;
        }

        const nextLeaderOptions = (employeePage.records || [])
          .filter((employee: EmployeeBrief) => {
            const isLeader = leaderIds.has(employee.id);
            const isHr = /hr|人力/i.test(employee.postName || '');
            return isLeader || isHr;
          })
          .map((employee: EmployeeBrief) => {
            const roleLabel = buildLeaderRoleLabel(
              employee.id,
              leaderIds,
              employee.postName,
            );
            return {
              label: roleLabel
                ? `${employee.employeeName}（${employee.deptName} · ${roleLabel}）`
                : `${employee.employeeName}（${employee.deptName}）`,
              value: employee.id,
            };
          })
          .filter(
            (option, index, array) =>
              array.findIndex((item) => item.value === option.value) === index,
          );

        setRealLeaderOptions(nextLeaderOptions);

        const currentLeaderId = form.getFieldValue('toLeaderId');
        if (
          currentLeaderId &&
          !nextLeaderOptions.some((option) => option.value === currentLeaderId)
        ) {
          form.setFieldsValue({ toLeaderId: undefined });
        }
      } catch (error) {
        if (!cancelled) {
          setRealLeaderOptions([]);
          message.error('新汇报人候选加载失败，请重新选择新部门后重试');
        }
      } finally {
        if (!cancelled) {
          setLeaderLoading(false);
        }
      }
    };

    void loadDeptRelatedOptions();

    return () => {
      cancelled = true;
    };
  }, [form, modalOpen, selectedToDeptId]);

  useEffect(() => {
    if (!modalOpen) {
      return;
    }

    if (!selectedToPostId) {
      form.setFieldsValue({ toJobLevel: undefined });
      return;
    }

    const currentJobLevel = form.getFieldValue('toJobLevel');
    if (jobLevelOptions.length === 1) {
      if (currentJobLevel !== jobLevelOptions[0].value) {
        form.setFieldsValue({ toJobLevel: jobLevelOptions[0].value });
      }
      return;
    }

    if (
      currentJobLevel &&
      jobLevelOptions.length &&
      !jobLevelOptions.some((item) => item.value === currentJobLevel)
    ) {
      form.setFieldsValue({ toJobLevel: undefined });
    }
  }, [form, jobLevelOptions, modalOpen, selectedToPostId]);

  useEffect(() => {
    if (!modalOpen || !employeeIdFromUrl) {
      return;
    }
    const employeeId = Number(employeeIdFromUrl);
    if (!employeeId || employeeId < 1) {
      return;
    }
    form.setFieldsValue({ employeeId });
    void handleEmployeeLookup(employeeId);
  }, [employeeIdFromUrl, form, modalOpen]);

  const columns: ProColumns<TransferApplication>[] = [
    {
      title: '员工姓名',
      dataIndex: 'keyword',
      hideInTable: true,
      fieldProps: {
        placeholder: '请输入员工姓名 / 工号',
        allowClear: true,
      },
    },
    {
      title: '原部门',
      dataIndex: 'departmentId',
      hideInTable: true,
      valueType: 'select',
      fieldProps: {
        options: departmentOptions,
        allowClear: true,
        showSearch: true,
        filterOption: departmentFilterOption,
        optionFilterProp: 'label',
        placeholder: '请输入部门名称',
      },
    },
    {
      title: '调岗状态',
      dataIndex: 'approvalStatus',
      hideInTable: true,
      valueType: 'select',
      valueEnum: {
        [ApprovalStatus.DRAFT]: { text: '草稿' },
        [ApprovalStatus.APPROVING]: { text: '审批中' },
        [ApprovalStatus.APPROVED]: { text: '已通过' },
        [ApprovalStatus.REJECTED]: { text: '已驳回' },
      },
    },
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      width: 140,
      search: false,
      renderText: (_, record) =>
        record.employeeName || `员工 ${record.employeeId}`,
    },
    {
      title: '原部门 / 新部门',
      dataIndex: 'fromDeptName',
      width: 180,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.fromDeptName || '-'}</span>
          <Text type="secondary">/ {record.toDeptName || '-'}</Text>
        </Space>
      ),
    },
    {
      title: '原职位 / 新职位',
      dataIndex: 'fromPostName',
      width: 200,
      search: false,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.fromPostName || '-'}</span>
          <Text type="secondary">/ {record.toPostName || '-'}</Text>
        </Space>
      ),
    },
    {
      title: '生效日期',
      dataIndex: 'effectiveDate',
      valueType: 'date',
      width: 120,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      width: 110,
      search: false,
      render: (_, record) => {
        const meta =
          statusMeta[record.approvalStatus ?? ApprovalStatus.DRAFT] ||
          statusMeta[0];
        return (
          <Tag color={meta.color}>{record.approvalStatusDesc || meta.text}</Tag>
        );
      },
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      width: 170,
      search: false,
      render: (_, record) => formatProcessDateTime(record.createTime),
    },
    {
      title: '操作',
      valueType: 'option',
      width: 120,
      render: (_, record) =>
        record.approvalStatus === ApprovalStatus.APPROVING ? (
          <Popconfirm
            title="快速审批通过调岗申请"
            description="确认后将直接完成当前调岗审批流程。"
            onConfirm={async () => {
              await quickApproveTransferApplication(record.id);
              message.success('已快速审批通过调岗申请');
              actionRef.current?.reload();
            }}
          >
            <Button size="small" type="primary">
              快速审批
            </Button>
          </Popconfirm>
        ) : null,
    },
  ];

  return (
    <PageContainer
      header={{
        title: '调岗申请',
        subTitle: '原岗位与新岗位对比、审批发起与生效跟踪',
      }}
    >
      <ProTable<TransferApplication>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        scroll={{ x: 1080 }}
        request={async (params) => {
          const result = await getTransferApplicationList({
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword: params.keyword as string,
            departmentId: params.departmentId as number,
            approvalStatus: params.approvalStatus as number,
          });
          return {
            data: result.records || [],
            total: result.total || 0,
            success: true,
          };
        }}
        search={{ labelWidth: 88, span: 8 }}
        pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        toolbar={{
          title: '调岗申请列表',
          actions: [
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalOpen(true)}
            >
              创建调岗申请
            </Button>,
          ],
        }}
      />

      <ModalForm<TransferFormValues>
        form={form}
        title="创建调岗申请"
        width={760}
        open={modalOpen}
        onOpenChange={(open) => {
          setModalOpen(open);
          if (!open) {
            form.resetFields();
            setCurrentEmployeeDetail(undefined);
            setRealPostOptions([]);
            setRealLeaderOptions([]);
            setEmployeeLoading(false);
            setPostLoading(false);
            setLeaderLoading(false);
          }
        }}
        modalProps={{ destroyOnClose: true, centered: true }}
        submitter={{ searchConfig: { submitText: '提交审批' } }}
        initialValues={{
          employeeId: employeeIdFromUrl ? Number(employeeIdFromUrl) : undefined,
        }}
        onFinish={async (values) => {
          const payload: TransferApplicationCreateRequest = {
            employeeId: values.employeeId,
            toDeptId: values.toDeptId,
            toPostId: values.toPostId,
            toJobLevel: values.toJobLevel,
            toLeaderId: values.toLeaderId,
            effectiveDate: values.effectiveDate,
            salaryAdjustment: values.salaryAdjustment,
            reason: values.reason,
          };
          await createTransferApplication(payload);
          message.success('调岗申请已提交');
          setModalOpen(false);
          actionRef.current?.reload();
          return true;
        }}
      >
        <ProFormGroup title="员工选择">
          <ProFormDigit
            name="employeeId"
            label="员工 ID"
            width="sm"
            min={1}
            rules={[{ required: true, message: '请输入调岗员工ID' }]}
            fieldProps={{
              onBlur: () => {
                triggerEmployeeLookup();
              },
              onPressEnter: (event) => {
                event.preventDefault();
                triggerEmployeeLookup();
              },
            }}
          />
          <ProFormText
            name="employeeName"
            label="员工姓名"
            width="md"
            fieldProps={{
              readOnly: true,
              placeholder: employeeLoading
                ? '员工信息加载中...'
                : '输入员工ID后自动带出',
            }}
          />
          <ProFormText
            name="employeeNo"
            label="员工工号"
            width="md"
            fieldProps={{
              readOnly: true,
              placeholder: employeeLoading
                ? '员工信息加载中...'
                : '输入员工ID后自动带出',
            }}
          />
        </ProFormGroup>

        <Row gutter={16}>
          <Col span={12}>
            <Card
              size="small"
              title="原岗位信息（只读）"
              style={{ minHeight: 252 }}
            >
              <ProFormText
                name="fromDeptName"
                label="原部门"
                fieldProps={{
                  readOnly: true,
                  placeholder: currentEmployeeDetail
                    ? '已自动带出员工原部门'
                    : '员工接口完善后自动带出',
                }}
              />
              <ProFormText
                name="fromPostName"
                label="原职位"
                fieldProps={{
                  readOnly: true,
                  placeholder: currentEmployeeDetail
                    ? '已自动带出员工原职位'
                    : '员工接口完善后自动带出',
                }}
              />
            </Card>
          </Col>
          <Col span={12}>
            <Card size="small" title="新岗位信息" style={{ minHeight: 252 }}>
              <ProFormSelect
                name="toDeptId"
                label="新部门"
                options={departmentOptions}
                rules={[{ required: true, message: '请选择新部门' }]}
                fieldProps={{
                  allowClear: true,
                  showSearch: true,
                  filterOption: departmentFilterOption,
                  optionFilterProp: 'label',
                  placeholder: '请选择新部门',
                }}
              />
              <ProFormSelect
                name="toPostId"
                label="新职位"
                options={postOptions}
                rules={[{ required: true, message: '请选择新职位' }]}
                fieldProps={{
                  disabled: !selectedToDeptId,
                  loading: postLoading,
                  allowClear: true,
                  showSearch: true,
                  optionFilterProp: 'label',
                  placeholder: selectedToDeptId
                    ? '请选择新职位'
                    : '请先选择新部门',
                }}
              />
              <ProFormText
                name="toJobLevel"
                label="新职级"
                fieldProps={{
                  readOnly: jobLevelOptions.length === 1,
                  placeholder: jobLevelPlaceholder,
                }}
              />
              <ProFormSelect
                name="toLeaderId"
                label="新汇报人"
                options={realLeaderOptions}
                fieldProps={{
                  disabled: !selectedToDeptId,
                  loading: leaderLoading,
                  allowClear: true,
                  showSearch: true,
                  optionFilterProp: 'label',
                  placeholder: selectedToDeptId
                    ? '请选择新汇报人'
                    : '请先选择新部门',
                }}
              />
            </Card>
          </Col>
        </Row>

        <ProFormGroup>
          <ProFormDatePicker
            name="effectiveDate"
            label="生效日期"
            width="md"
            rules={[{ required: true, message: '请选择生效日期' }]}
          />
          <ProFormDigit
            name="salaryAdjustment"
            label="薪资调整"
            width="sm"
            fieldProps={{ precision: 2 }}
          />
        </ProFormGroup>
        <ProFormTextArea
          name="reason"
          label="调岗原因"
          fieldProps={{ rows: 4, maxLength: 200, showCount: true }}
          rules={[{ required: true, message: '请输入调岗原因' }]}
        />
      </ModalForm>
    </PageContainer>
  );
};

export default TransferPage;

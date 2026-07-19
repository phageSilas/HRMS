/**
 * 审批详情页面（卡片式布局）
 *
 * 展示申请信息、审批流程时间轴、当前节点操作
 */
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Card,
  Row,
  Col,
  Tag,
  Timeline,
  Spin,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Avatar,
  Breadcrumb,
} from 'antd';
import {
  UserOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  SwapOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import {
  getApprovalDetail,
  operateApproval,
} from '@/services/approval';
import type { ApprovalDetail, ApprovalNode } from '@/services/approval';
import { getEmployeeList } from '@/services/employee';
import type { EmployeeBrief } from '@/services/employee';

// ============ 业务类型表单字段中文映射 ============

const FORM_LABEL_MAP: Record<string, Record<string, string>> = {
  LEAVE_REQUEST: {
    leaveType: '请假类型',
    startTime: '开始时间',
    endTime: '结束时间',
    totalDays: '请假天数',
    totalHours: '请假小时数',
    leaveReason: '请假事由',
  },
  CORRECTION: {
    correctionDate: '补卡日期',
    correctionType: '补卡类型',
    correctionReason: '补卡原因',
  },
  OVERTIME: {
    overtimeDate: '加班日期',
    duration: '加班时长（小时）',
    reason: '加班事由',
  },
  SALARY: {
    yearMonth: '核算月份',
    batchName: '批次名称',
    remark: '备注',
  },
  ENTRY: {
    employeeName: '员工姓名',
    deptName: '部门',
    postName: '职位',
    hireDate: '入职日期',
    salary: '薪资',
    contractType: '合同类型',
    remark: '备注',
  },
  REGULAR: {
    employeeName: '员工姓名',
    probationEndDate: '试用期结束日期',
    evaluation: '评估意见',
  },
  TRANSFER: {
    employeeName: '员工姓名',
    fromDept: '原部门',
    toDept: '目标部门',
    fromPost: '原职位',
    toPost: '目标职位',
    reason: '调岗原因',
  },
  LEAVE: {
    employeeName: '员工姓名',
    leaveDate: '离职日期',
    leaveType: '离职类型',
    reason: '离职原因',
  },
};

/** 补卡类型值映射 */
const CORRECTION_TYPE_MAP: Record<string, string> = {
  CLOCK_IN: '上班卡',
  CLOCK_OUT: '下班卡',
};

/** 请假类型值映射 */
const LEAVE_TYPE_MAP: Record<string, string> = {
  ANNUAL: '年假',
  COMPASSIONATE: '调休',
  SICK: '病假',
  PERSONAL: '事假',
  MARRIAGE: '婚假',
  MATERNITY: '产假',
  FUNERAL: '丧假',
};

/** 审批状态颜色映射 */
const STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  DRAFT: 'default',
  WITHDRAWN: 'warning',
  CANCELLED: 'default',
  EXPIRED: 'warning',
};

/** 业务类型标签颜色映射 */
const BUSINESS_TYPE_COLOR_MAP: Record<string, string> = {
  ENTRY: 'green',
  REGULAR: 'blue',
  TRANSFER: 'purple',
  LEAVE: 'red',
  LEAVE_REQUEST: 'orange',
  CORRECTION: 'cyan',
  OVERTIME: 'geekblue',
  SALARY: 'magenta',
};

/**
 * 根据业务类型和字段名获取中文标签
 */
const getFormLabel = (businessType: string, key: string): string => {
  const typeMap = FORM_LABEL_MAP[businessType];
  return typeMap?.[key] || key;
};

/**
 * 根据业务类型和字段名格式化值
 */
const formatFormValue = (businessType: string, key: string, value: any): string => {
  if (value === null || value === undefined || value === '') return '-';

  if (businessType === 'CORRECTION' && key === 'correctionType') {
    return CORRECTION_TYPE_MAP[String(value)] || String(value);
  }

  if (businessType === 'LEAVE_REQUEST' && key === 'leaveType') {
    return LEAVE_TYPE_MAP[String(value)] || String(value);
  }

  return String(value);
};

// ============ 页面组件 ============

const DetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<ApprovalDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [operateModal, setOperateModal] = useState<{
    visible: boolean;
    action: 'approve' | 'reject' | 'transfer';
  }>({ visible: false, action: 'approve' });
  const [operateLoading, setOperateLoading] = useState<boolean>(false);
  const [operateForm] = Form.useForm();
  const [employeeOptions, setEmployeeOptions] = useState<EmployeeBrief[]>([]);
  const [searching, setSearching] = useState<boolean>(false);
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ============ 数据加载 ============

  const fetchDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setLoadError(null);
    try {
      const data = await getApprovalDetail(Number(id));
      setDetail(data);
    } catch (err: any) {
      setLoadError(err?.message || '获取审批详情失败');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  // ============ 员工搜索 ============

  const handleEmployeeSearch = useCallback((keyword: string) => {
    if (searchTimerRef.current) {
      clearTimeout(searchTimerRef.current);
    }
    if (!keyword || keyword.length < 1) {
      setEmployeeOptions([]);
      return;
    }
    searchTimerRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const result = await getEmployeeList({ keyword, pageNum: 1, pageSize: 20 });
        setEmployeeOptions(result.records || []);
      } catch {
        setEmployeeOptions([]);
      } finally {
        setSearching(false);
      }
    }, 300);
  }, []);

  // ============ 审批操作 ============

  const handleOperate = useCallback(async () => {
    try {
      const values = await operateForm.validateFields();
      setOperateLoading(true);
      await operateApproval(Number(detail?.currentTaskId), {
        action: operateModal.action,
        comment: values.comment,
        targetUserId: values.targetUserId
          ? Number(values.targetUserId)
          : undefined,
      });
      message.success('操作成功');
      setOperateModal({ visible: false, action: 'approve' });
      operateForm.resetFields();
      fetchDetail();
    } catch (error: any) {
      if (error?.errorFields) return;
      message.error(error?.message || '操作失败');
    } finally {
      setOperateLoading(false);
    }
  }, [detail, operateModal.action, operateForm, fetchDetail]);

  const showOperateModal = (action: 'approve' | 'reject' | 'transfer') => {
    operateForm.resetFields();
    setOperateModal({ visible: true, action });
  };

  const closeOperateModal = () => {
    setOperateModal({ visible: false, action: 'approve' });
    operateForm.resetFields();
  };

  // ============ 渲染函数 ============

  /** 渲染面包屑 + 单号 */
  const renderBreadcrumb = () => (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 24,
      }}
    >
      <Breadcrumb
        items={[
          {
            title: (
              <span
                style={{ cursor: 'pointer', color: '#1890ff' }}
                onClick={() => history.push('/approval/workspace')}
              >
                审批工作台
              </span>
            ),
          },
          { title: '审批详情' },
        ]}
      />
      {detail && (
        <span style={{ fontSize: 13, color: '#8c8c8c', fontWeight: 500 }}>
          单号：APR-{String(detail.id).padStart(4, '0')}
        </span>
      )}
    </div>
  );

  /** 渲染申请人信息卡片 */
  const renderApplicantCard = () => {
    if (!detail) return null;
    const isPending = detail.status === 'PENDING';

    return (
      <Card
        style={{
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          marginBottom: 16,
        }}
        bodyStyle={{ padding: '24px' }}
      >
        {/* 头部：头像 + 姓名 + 部门 + 状态 */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 20,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Avatar
              size={48}
              icon={<UserOutlined />}
              style={{ backgroundColor: '#1890ff' }}
            >
              {detail.applicantName?.[0]}
            </Avatar>
            <div>
              <div style={{ fontSize: 16, fontWeight: 600, color: '#262626' }}>
                {detail.applicantName}
              </div>
              {/* 部门信息暂从接口获取，目前未返回时显示 '-' */}
              <div style={{ fontSize: 13, color: '#8c8c8c' }}>-</div>
            </div>
          </div>
          <Tag
            color={isPending ? 'orange' : STATUS_COLOR_MAP[detail.status] || 'default'}
            style={{
              borderRadius: 4,
              padding: '2px 12px',
              fontSize: 13,
              fontWeight: 500,
            }}
          >
            {detail.statusName || detail.status}
          </Tag>
        </div>

        {/* 三列信息 */}
        <Row gutter={24}>
          <Col span={8}>
            <div style={{ fontSize: 13, color: '#8c8c8c', marginBottom: 4 }}>审批类型</div>
            <Tag
              color={BUSINESS_TYPE_COLOR_MAP[detail.businessType] || 'default'}
              style={{ borderRadius: 4, fontSize: 13 }}
            >
              {detail.businessTypeName}
            </Tag>
          </Col>
          <Col span={8}>
            <div style={{ fontSize: 13, color: '#8c8c8c', marginBottom: 4 }}>申请时间</div>
            <div style={{ fontSize: 14, color: '#595959' }}>{detail.createdAt}</div>
          </Col>
          <Col span={8}>
            <div style={{ fontSize: 13, color: '#8c8c8c', marginBottom: 4 }}>截止时间</div>
            <div style={{ fontSize: 14, color: '#595959' }}>
              {/* 接口暂无 deadline，显示 '-' */}
              -
            </div>
          </Col>
        </Row>
      </Card>
    );
  };

  /** 渲染申请详情卡片（两列布局） */
  const renderFormDataCard = () => {
    if (!detail) return null;
    const formData = detail.formData;
    const entries = formData ? Object.entries(formData) : [];

    return (
      <Card
        title={
          <span style={{ fontSize: 15, fontWeight: 600, color: '#262626' }}>
            申请详情
          </span>
        }
        style={{
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          marginBottom: 16,
        }}
        headStyle={{ borderBottom: '1px solid #f0f0f0', padding: '16px 24px' }}
        bodyStyle={{ padding: '24px' }}
      >
        {entries.length > 0 ? (
          <Row gutter={[24, 16]}>
            {entries.map(([key, value], index) => (
              <Col span={12} key={key}>
                <div
                  style={{
                    display: 'flex',
                    padding: '8px 0',
                    borderBottom: index < entries.length - 1 ? '1px dashed #f0f0f0' : 'none',
                  }}
                >
                  <div
                    style={{
                      width: 100,
                      flexShrink: 0,
                      color: '#8c8c8c',
                      fontSize: 14,
                    }}
                  >
                    {getFormLabel(detail.businessType, key)}
                  </div>
                  <div style={{ color: '#262626', fontSize: 14, fontWeight: 450 }}>
                    {formatFormValue(detail.businessType, key, value)}
                  </div>
                </div>
              </Col>
            ))}
          </Row>
        ) : (
          <div style={{ color: '#999', textAlign: 'center', padding: '20px 0' }}>
            暂无申请内容
          </div>
        )}
      </Card>
    );
  };

  /** 渲染审批流程时间轴 */
  const renderApprovalTimeline = () => {
    if (!detail) return null;
    const nodes = detail.approvalNodes || [];

    if (nodes.length === 0) {
      return (
        <Card
          title={
            <span style={{ fontSize: 15, fontWeight: 600, color: '#262626' }}>
              审批流程
            </span>
          }
          style={{
            borderRadius: 12,
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            marginBottom: 16,
          }}
          headStyle={{ borderBottom: '1px solid #f0f0f0', padding: '16px 24px' }}
          bodyStyle={{ padding: '24px' }}
        >
          <div style={{ color: '#999', textAlign: 'center', padding: '20px 0' }}>
            暂无审批节点
          </div>
        </Card>
      );
    }

    const timelineItems = nodes.map((node: ApprovalNode, index: number) => {
      const isCurrent = node.status === 'current';
      const isCompleted = node.status === 'completed';
      const isPending = node.status === 'pending' || (!isCurrent && !isCompleted);

      // 根据状态确定颜色和图标
      let dotColor: string;
      let dotIcon: React.ReactNode;

      if (isCompleted) {
        dotColor = 'green';
        dotIcon = <CheckCircleOutlined style={{ fontSize: 16, color: '#52c41a' }} />;
      } else if (isCurrent) {
        dotColor = 'orange';
        dotIcon = <ClockCircleOutlined style={{ fontSize: 16, color: '#faad14' }} />;
      } else {
        dotColor = 'gray';
        dotIcon = undefined;
      }

      return {
        color: dotColor,
        dot: dotIcon,
        children: (
          <div
            style={{
              paddingBottom: isCurrent ? 8 : 4,
              opacity: isPending && !isCurrent ? 0.5 : 1,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
              <Avatar
                size={28}
                icon={<UserOutlined />}
                style={{
                  backgroundColor: isCompleted ? '#52c41a' : isCurrent ? '#faad14' : '#d9d9d9',
                  flexShrink: 0,
                }}
              >
                {node.operatorName?.[0]}
              </Avatar>
              <div>
                <span style={{ fontWeight: 500, fontSize: 14, color: '#262626' }}>
                  {node.operatorName || '待处理'}
                </span>
                <span style={{ color: '#8c8c8c', fontSize: 13, marginLeft: 8 }}>
                  {node.nodeName}
                </span>
              </div>
            </div>
            {isCurrent && (
              <div style={{ marginLeft: 38, marginTop: 4 }}>
                <Tag color="orange" style={{ borderRadius: 4, fontSize: 12 }}>
                  等待审批中...
                </Tag>
              </div>
            )}
            {isCompleted && (
              <div style={{ marginLeft: 38, marginTop: 2 }}>
                <Tag color="green" style={{ borderRadius: 4, fontSize: 12 }}>
                  已审批
                </Tag>
              </div>
            )}
          </div>
        ),
      };
    });

    return (
      <Card
        title={
          <span style={{ fontSize: 15, fontWeight: 600, color: '#262626' }}>
            审批流程
          </span>
        }
        style={{
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          marginBottom: 16,
        }}
        headStyle={{ borderBottom: '1px solid #f0f0f0', padding: '16px 24px' }}
        bodyStyle={{ padding: '24px' }}
      >
        <Timeline items={timelineItems} />
      </Card>
    );
  };

  /** 渲染审批操作区 */
  const renderOperationArea = () => {
    if (!detail || !detail.currentOperator) return null;

    return (
      <Card
        title={
          <span style={{ fontSize: 15, fontWeight: 600, color: '#262626' }}>
            审批操作
          </span>
        }
        style={{
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
        }}
        headStyle={{ borderBottom: '1px solid #f0f0f0', padding: '16px 24px' }}
        bodyStyle={{ padding: '24px' }}
      >
        <Space size={16}>
          <Button
            style={{
              borderRadius: 8,
              borderColor: '#52c41a',
              color: '#52c41a',
              borderWidth: 1.5,
              height: 38,
              paddingInline: 24,
            }}
            onClick={() => showOperateModal('approve')}
          >
            <CheckCircleOutlined /> 通过
          </Button>
          <Button
            style={{
              borderRadius: 8,
              borderColor: '#ff4d4f',
              color: '#ff4d4f',
              borderWidth: 1.5,
              height: 38,
              paddingInline: 24,
            }}
            onClick={() => showOperateModal('reject')}
          >
            <CloseCircleOutlined /> 拒绝
          </Button>
          <Button
            style={{
              borderRadius: 8,
              borderColor: '#1890ff',
              color: '#1890ff',
              borderWidth: 1.5,
              height: 38,
              paddingInline: 24,
            }}
            onClick={() => showOperateModal('transfer')}
          >
            <SwapOutlined /> 转交
          </Button>
        </Space>
      </Card>
    );
  };

  /** 操作弹窗表单 */
  const renderOperateFormItems = () => {
    const items: React.ReactNode[] = [];

    if (operateModal.action === 'transfer') {
      items.push(
        <Form.Item
          key="targetUserId"
          name="targetUserId"
          label="目标审批人"
          rules={[{ required: true, message: '请选择目标审批人' }]}
        >
          <Select
            showSearch
            placeholder="输入姓名或工号搜索员工"
            filterOption={false}
            notFoundContent={searching ? '搜索中...' : '未找到匹配员工'}
            loading={searching}
            onSearch={handleEmployeeSearch}
            labelInValue={false}
            options={employeeOptions.map((emp) => ({
              label: `${emp.employeeName}（${emp.employeeNo}）- ${emp.deptName || ''}${emp.postName ? '/' + emp.postName : ''}`,
              value: emp.id,
            }))}
          />
        </Form.Item>,
      );
    }

    items.push(
      <Form.Item
        key="comment"
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
      </Form.Item>,
    );

    return items;
  };

  // ============ 加载状态 ============

  if (loading) {
    return (
      <>
        {renderBreadcrumb()}
        <div style={{ textAlign: 'center', padding: '120px 0' }}>
          <Spin size="large" />
        </div>
      </>
    );
  }

  // ============ 错误状态 ============

  if (loadError && !loading) {
    return (
      <>
        {renderBreadcrumb()}
        <Card style={{ borderRadius: 12 }}>
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
            <div style={{ fontSize: 48, marginBottom: 16 }}>😵</div>
            <div style={{ fontSize: 16, marginBottom: 8 }}>{loadError}</div>
            <Button icon={<ReloadOutlined />} onClick={fetchDetail} style={{ borderRadius: 8 }}>
              重新加载
            </Button>
          </div>
        </Card>
      </>
    );
  }

  // ============ 渲染主内容 ============

  return (
    <PageContainer>
      {/* 面包屑 + 单号 */}
      {renderBreadcrumb()}

      {/* 申请人信息卡片 */}
      {renderApplicantCard()}

      {/* 申请详情卡片 */}
      {renderFormDataCard()}

      {/* 审批流程时间轴 */}
      {renderApprovalTimeline()}

      {/* 审批操作区 */}
      {renderOperationArea()}

      {/* 操作弹窗 */}
      <Modal
        title={
          operateModal.action === 'approve'
            ? '审批通过'
            : operateModal.action === 'reject'
              ? '审批拒绝'
              : '转交处理人'
        }
        open={operateModal.visible}
        onOk={handleOperate}
        onCancel={closeOperateModal}
        confirmLoading={operateLoading}
        destroyOnClose
      >
        <Form form={operateForm} layout="vertical">
          {renderOperateFormItems()}
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default DetailPage;

/**
 * 审批详情页面（卡片式布局）
 *
 * 展示审批申请的详细信息，包括：
 * - 申请人信息卡片（头像、姓名、状态、审批类型、时间）
 * - 业务表单详情（两列布局，根据 businessType 动态渲染字段中文名）
 * - 审批流程时间轴（各节点状态及操作人）
 * - 当前审批人操作区（通过 / 拒绝 / 转交）
 * - 加载态 / 错误态处理
 *
 * 数据流：从路由参数获取 :id → 调用 getApprovalDetail 加载详情，
 *         当前审批人可发起操作 → 调用 operateApproval 提交。
 *
 * @module ApprovalDetail
 */

import React, { useState, useEffect, useCallback } from 'react';
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
import { BUSINESS_TYPE_COLOR_MAP } from '@/constants/enums';
import { useEmployeeSearch } from '@/hooks/useEmployeeSearch';
import { getErrorMessage } from '@/utils/error';

// ============ 业务类型表单字段中文映射 ============

/**
 * 各业务类型的表单字段中文名映射
 *
 * key 为业务类型编码，value 为该类型下各字段中文名。
 * 用于将后端返回的 formData 字段 key 转换为可读的中文标签。
 */
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

/** 审批状态 → Ant Design Tag 颜色映射 */
const STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  DRAFT: 'default',
  WITHDRAWN: 'warning',
  CANCELLED: 'default',
  EXPIRED: 'warning',
};

/**
 * 根据业务类型和字段名获取中文标签
 *
 * @param businessType 业务类型编码
 * @param key          表单字段 key
 * @returns 中文字段名，未匹配时返回原 key
 */
const getFormLabel = (businessType: string, key: string): string => {
  const typeMap = FORM_LABEL_MAP[businessType];
  return typeMap?.[key] || key;
};

/**
 * 根据业务类型和字段名格式化表单值为展示文本
 *
 * 对特殊字段（如补卡类型、请假类型）做值映射转换，
 * 空值统一显示为 "-"。
 *
 * @param businessType 业务类型编码
 * @param key          表单字段 key
 * @param value        表单原始值
 * @returns 格式化后的展示文本
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

/** 审批详情主页面 */
const DetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<ApprovalDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  /** 操作弹窗状态 */
  const [operateModal, setOperateModal] = useState<{
    visible: boolean;
    action: 'approve' | 'reject' | 'transfer';
  }>({ visible: false, action: 'approve' });
  const [operateLoading, setOperateLoading] = useState<boolean>(false);
  const [operateForm] = Form.useForm();
  /** 转交目标员工选项列表 */
  const { options: employeeOptions, searching, search: handleEmployeeSearch } = useEmployeeSearch();

  // ============ 数据加载 ============

  /**
   * 加载审批详情
   *
   * 从 API 获取审批详细数据，成功后更新 detail 状态，
   * 失败时设置 loadError 供 UI 展示错误提示。
   */
  const fetchDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setLoadError(null);
    try {
      const data = await getApprovalDetail(Number(id));
      setDetail(data);
    } catch (err: unknown) {
      setLoadError(getErrorMessage(err, '获取审批详情失败'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  // ============ 审批操作 ============

  /**
   * 执行审批操作（通过 / 拒绝 / 转交）
   *
   * 先校验操作弹窗表单，然后调用 operateApproval API，
   * 成功后自动刷新详情页并关闭弹窗。
   * 表单校验失败时（Ant Design 错误）不处理。
   */
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
    } catch (err: unknown) {
      // Ant Design 表单校验失败时 error 包含 errorFields，此时不额外提示
      if (err && typeof err === 'object' && 'errorFields' in err) return;
      message.error(getErrorMessage(err, '操作失败'));
    } finally {
      setOperateLoading(false);
    }
  }, [detail, operateModal.action, operateForm, fetchDetail]);

  /** 打开操作弹窗 */
  const showOperateModal = (action: 'approve' | 'reject' | 'transfer') => {
    operateForm.resetFields();
    setOperateModal({ visible: true, action });
  };

  /** 关闭操作弹窗 */
  const closeOperateModal = () => {
    setOperateModal({ visible: false, action: 'approve' });
    operateForm.resetFields();
  };

  // ============ 渲染函数 ============

  /** 渲染面包屑导航 + 审批单号 */
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

  /** 渲染申请人信息卡片（头像、姓名、状态、审批类型、时间） */
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
        {/* 头部：头像 + 姓名 + 状态 */}
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

        {/* 三列信息：审批类型 / 申请时间 / 截止时间 */}
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

  /** 渲染申请详情卡片（两列布局，动态渲染表单字段） */
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

    // 将每个审批节点映射为 Timeline.Item 配置
    const timelineItems = nodes.map((node: ApprovalNode, index: number) => {
      const isCurrent = node.status === 'current';
      const isCompleted = node.status === 'completed';
      const isPending = node.status === 'pending' || (!isCurrent && !isCompleted);

      // 根据节点状态确定时间轴圆点的颜色和图标
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

  /** 渲染审批操作区（仅当前审批人可见） */
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

  /** 渲染操作弹窗表单（转交时显示员工搜索，拒绝时必填意见） */
  const renderOperateFormItems = () => {
    const items: React.ReactNode[] = [];

    // 转交操作时需要选择目标审批人
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

    // 审批意见字段（拒绝时必填）
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
      {renderBreadcrumb()}
      {renderApplicantCard()}
      {renderFormDataCard()}
      {renderApprovalTimeline()}
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

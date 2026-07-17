/**
 * 审批详情页面
 *
 * 展示审批单的详细信息、审批进度、审批历史，
 * 当前审批人可进行操作（通过/拒绝/转交）
 */
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Card,
  Descriptions,
  Steps,
  Timeline,
  Spin,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Tag,
} from 'antd';
import {
  getApprovalDetail,
  operateApproval,
} from '@/services/approval';
import type { ApprovalDetail } from '@/services/approval';
import { ReloadOutlined } from '@ant-design/icons';
import { getEmployeeList } from '@/services/employee';
import type { EmployeeBrief } from '@/services/employee';

// ============ 业务类型表单字段中文映射 ============

/** 各业务类型对应的表单字段中文标签 */
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

  // 补卡类型转中文
  if (businessType === 'CORRECTION' && key === 'correctionType') {
    return CORRECTION_TYPE_MAP[String(value)] || String(value);
  }

  // 请假类型转中文
  if (businessType === 'LEAVE_REQUEST' && key === 'leaveType') {
    return LEAVE_TYPE_MAP[String(value)] || String(value);
  }

  return String(value);
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

  /** 输入姓名/工号搜索员工（300ms 防抖） */
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

  /** 获取审批详情 */
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

  /** 执行审批操作 */
  const handleOperate = useCallback(async () => {
    try {
      const values = await operateForm.validateFields();
      setOperateLoading(true);
      await operateApproval(Number(detail.currentTaskId), {
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
      // 表单校验不通过时不处理
      if (error?.errorFields) return;
      message.error(error?.message || '操作失败');
    } finally {
      setOperateLoading(false);
    }
  }, [id, operateModal.action, operateForm, fetchDetail]);

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

  // ============ 审批进度（Steps） ============

  const stepsCurrent =
    detail?.approvalNodes.findIndex((n) => n.status === 'current') ?? -1;

  const stepsItems =
    detail?.approvalNodes.map((node) => ({
      title: node.nodeName,
      status:
        node.status === 'completed'
          ? 'finish'
          : node.status === 'current'
            ? 'process'
            : ('wait' as const),
      description: node.operatorName || undefined,
    })) ?? [];

  // ============ 审批历史（Timeline） ============

  const timelineItems =
    detail?.approvalHistory.map((item) => ({
      color:
        item.action === 'approve'
          ? 'green'
          : item.action === 'reject'
            ? 'red'
            : 'blue',
      children: (
        <div>
          <div>
            <strong>{item.operatorName}</strong> 在{' '}
            <strong>{item.nodeName}</strong> 节点 {item.actionName}
          </div>
          {item.comment && (
            <div style={{ marginTop: 4 }}>意见：{item.comment}</div>
          )}
          <div style={{ color: '#999', fontSize: 12, marginTop: 4 }}>
            {item.operatedAt}
          </div>
        </div>
      ),
    })) ?? [];

  // ============ 操作弹窗配置 ============

  const modalTitleMap: Record<string, string> = {
    approve: '审批通过',
    reject: '审批拒绝',
    transfer: '转交处理人',
  };

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

  // ============ 渲染 ============

  return (
    <PageContainer>
      <Spin spinning={loading}>
        {loadError && !loading && (
          <Card>
            <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>😵</div>
              <div style={{ fontSize: 16, marginBottom: 8 }}>{loadError}</div>
              <Button
                icon={<ReloadOutlined />}
                onClick={fetchDetail}
              >
                重新加载
              </Button>
            </div>
          </Card>
        )}
        {detail && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* 基础信息 */}
            <Card title="基本信息">
              <Descriptions column={2}>
                <Descriptions.Item label="申请标题">
                  {detail.title}
                </Descriptions.Item>
                <Descriptions.Item label="审批状态">
                  <Tag color={STATUS_COLOR_MAP[detail.status] || 'default'}>
                    {detail.statusName}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="业务类型">
                  {detail.businessTypeName}
                </Descriptions.Item>
                <Descriptions.Item label="申请人">
                  {detail.applicantName}
                </Descriptions.Item>
                <Descriptions.Item label="申请时间">
                  {detail.createdAt}
                </Descriptions.Item>
              </Descriptions>
            </Card>

            {/* 申请内容 */}
            <Card title="申请内容">
              {detail.formData && Object.entries(detail.formData).length > 0 ? (
                <Descriptions column={1}>
                  {Object.entries(detail.formData).map(([key, value]) => (
                    <Descriptions.Item
                      label={getFormLabel(detail.businessType, key)}
                      key={key}
                    >
                      {formatFormValue(detail.businessType, key, value)}
                    </Descriptions.Item>
                  ))}
                </Descriptions>
              ) : (
                <div style={{ color: '#999' }}>暂无申请内容</div>
              )}
            </Card>

            {/* 审批进度 */}
            <Card title="审批进度">
              {stepsItems.length > 0 ? (
                <Steps
                  current={
                    stepsCurrent >= 0 ? stepsCurrent : stepsItems.length
                  }
                  items={stepsItems}
                />
              ) : (
                <div style={{ color: '#999' }}>暂无审批节点</div>
              )}
            </Card>

            {/* 审批历史 */}
            <Card title="审批历史">
              {timelineItems.length > 0 ? (
                <Timeline items={timelineItems} />
              ) : (
                <div style={{ color: '#999' }}>暂无审批记录</div>
              )}
            </Card>

            {/* 审批操作 — 仅当前待办人可见 */}
            {detail.currentOperator && (
              <Card title="审批操作">
                <Space>
                  <Button
                    type="primary"
                    onClick={() => showOperateModal('approve')}
                  >
                    通过
                  </Button>
                  <Button danger onClick={() => showOperateModal('reject')}>
                    拒绝
                  </Button>
                  <Button onClick={() => showOperateModal('transfer')}>
                    转交
                  </Button>
                </Space>
              </Card>
            )}

            {/* 操作弹窗 */}
            <Modal
              title={modalTitleMap[operateModal.action]}
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
          </div>
        )}
      </Spin>
    </PageContainer>
  );
};

export default DetailPage;

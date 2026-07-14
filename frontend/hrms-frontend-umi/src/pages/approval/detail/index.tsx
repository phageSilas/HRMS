/**
 * 审批详情页面
 *
 * 展示审批单的详细信息、审批进度、审批历史，
 * 当前审批人可进行操作（通过/拒绝/转交）
 */
import React, { useState, useEffect, useCallback } from 'react';
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
  message,
  Tag,
} from 'antd';
import {
  getApprovalDetail,
  operateApproval,
} from '@/services/approval';
import type { ApprovalDetail } from '@/services/approval';

/** 审批状态颜色映射 */
const STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  DRAFT: 'default',
  WITHDRAWN: 'warning',
  CANCELLED: 'default',
};

const DetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<ApprovalDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [operateModal, setOperateModal] = useState<{
    visible: boolean;
    action: 'approve' | 'reject' | 'transfer';
  }>({ visible: false, action: 'approve' });
  const [operateLoading, setOperateLoading] = useState<boolean>(false);
  const [operateForm] = Form.useForm();

  /** 获取审批详情 */
  const fetchDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await getApprovalDetail(Number(id));
      setDetail(data);
    } catch {
      message.error('获取审批详情失败');
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
      await operateApproval(Number(id), {
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
          label="目标用户 ID"
          rules={[{ required: true, message: '请输入目标用户 ID' }]}
        >
          <Input placeholder="请输入目标用户 ID" />
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
                    <Descriptions.Item label={key} key={key}>
                      {String(value ?? '')}
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

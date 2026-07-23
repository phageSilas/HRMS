/**
 * 我的请假页面
 * 假期余额概览（三卡片）+ 申请记录列表 + 申请/取消请假
 */

import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  message,
  Modal,
  Progress,
  Row,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd';
import dayjs from 'dayjs';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { cancelLeave, createLeave, getLeaveBalance, getLeaveList } from '@/services/profile';
import type { LeaveBalanceVO, LeaveRequestDTO, LeaveVO } from '@/services/profile';
import LeaveModal from '@/pages/profile/attendance/components/LeaveModal';

const { Text, Title, Paragraph } = Typography;

// ============ 常量 ============

/** 假期类型 → Tag 颜色 */
const LEAVE_TYPE_TAG_COLOR: Record<string, string> = {
  ANNUAL: 'blue',
  COMPASSIONATE: 'purple',
  SICK: 'red',
  PERSONAL: 'orange',
  MARRIAGE: 'pink',
  MATERNITY: 'cyan',
  FUNERAL: 'default',
};

/** 审批状态 → Tag 颜色/文字（仅显示关键状态） */
const STATUS_TAG_MAP: Record<number, { text: string; color: string }> = {
  1: { text: '审批中', color: 'gold' },
  2: { text: '已批准', color: 'green' },
  3: { text: '已拒绝', color: 'red' },
  4: { text: '已取消', color: 'default' },
};

/** 余额卡片配置 */
const BALANCE_CARD_CONFIG = [
  {
    key: 'annual',
    title: '年假余额',
    totalField: 'annualTotal' as const,
    usedField: 'annualUsed' as const,
    remainingField: 'annualRemaining' as const,
    bgColor: '#e6f4ff',
    strokeColor: '#1677ff',
  },
  {
    key: 'sick',
    title: '病假余额',
    totalField: 'sickTotal' as const,
    usedField: 'sickUsed' as const,
    remainingField: 'sickRemaining' as const,
    bgColor: '#fff1f0',
    strokeColor: '#ff4d4f',
  },
  {
    key: 'compassionate',
    title: '调休余额',
    totalField: 'compassionateTotal' as const,
    usedField: 'compassionateUsed' as const,
    remainingField: 'compassionateRemaining' as const,
    bgColor: '#f9f0ff',
    strokeColor: '#722ed1',
  },
] as const;

// ============ 子组件 ============

/** 单个余额卡片 */
const BalanceCard: React.FC<{
  config: (typeof BALANCE_CARD_CONFIG)[number];
  balance: Record<string, number | undefined>;
  loading: boolean;
}> = ({ config, balance, loading }) => {
  const total = balance[config.totalField] ?? 0;
  const used = balance[config.usedField] ?? 0;
  const remaining = balance[config.remainingField] ?? 0;
  const percent = total > 0 ? Math.round((remaining / total) * 100) : 0;

  return (
    <Col xs={24} sm={8}>
      <Card bordered={false} style={{ borderRadius: 12, background: config.bgColor, height: '100%' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '20px 0' }}>
            <Spin />
          </div>
        ) : (
          <div style={{ textAlign: 'center' }}>
            <Text type="secondary" style={{ fontSize: 13 }}>
              {config.title}
            </Text>
            <div style={{ fontSize: 36, fontWeight: 700, color: config.strokeColor, lineHeight: 1.3 }}>
              {remaining}
              <span style={{ fontSize: 14, color: '#999', fontWeight: 400 }}> 天</span>
            </div>
            <Progress
              percent={percent}
              strokeColor={config.strokeColor}
              showInfo={false}
              style={{ margin: '8px 0' }}
              size="small"
            />
            <Text type="secondary" style={{ fontSize: 12 }}>
              剩余 {remaining} 天 / 共 {total} 天
            </Text>
          </div>
        )}
      </Card>
    </Col>
  );
};

/** 单个请假记录卡片 */
const LeaveRecordCard: React.FC<{
  record: LeaveVO;
  onCancel: (id: number) => void;
}> = ({ record, onCancel }) => {
  const leaveTypeColor = LEAVE_TYPE_TAG_COLOR[record.leaveType] || 'default';
  const statusInfo = STATUS_TAG_MAP[record.approvalStatus];
  const isPending = record.approvalStatus === 1;

  const formatDate = (dateStr?: string) =>
    dateStr ? dayjs(dateStr).format('YYYY-MM-DD') : '-';

  const startDate = formatDate(record.startTime);
  const endDate = formatDate(record.endTime);

  return (
    <Card
      bordered={false}
      style={{ borderRadius: 8, marginBottom: 12 }}
      styles={{ body: { padding: '16px 20px' } }}
    >
      <Row align="middle" wrap gutter={[16, 8]}>
        {/* 左侧信息 */}
        <Col flex="auto">
          <Space direction="vertical" size={6} style={{ width: '100%' }}>
            {/* 标签行 */}
            <Space>
              <Tag color={leaveTypeColor}>{record.leaveTypeDesc}</Tag>
              {statusInfo && <Tag color={statusInfo.color}>{statusInfo.text}</Tag>}
            </Space>

            {/* 时间行 */}
            <Text>
              {startDate} ~ {endDate}
              <Text type="secondary"> 共 {record.totalDays} 天</Text>
            </Text>

            {/* 理由行 */}
            {record.leaveReason && (
              <Paragraph
                style={{ margin: 0, color: '#333' }}
                ellipsis={{ rows: 1, expandable: true, symbol: '展开' }}
              >
                {record.leaveReason}
              </Paragraph>
            )}
          </Space>
        </Col>

        {/* 右侧操作 */}
        <Col>
          <Space direction="vertical" size={8}>
            {record.approvalInstanceId && (
              <Button
                type="default"
                size="small"
                onClick={() => history.push(`/approval/detail/${record.approvalInstanceId}`)}
              >
                查看进度
              </Button>
            )}
            {isPending && (
              <Button
                danger
                type="default"
                size="small"
                onClick={() => {
                  Modal.confirm({
                    title: '确认取消申请',
                    content: '确定要取消该请假申请吗？取消后不可恢复。',
                    okText: '确认取消',
                    cancelText: '再想想',
                    onOk: () => onCancel(record.id),
                  });
                }}
              >
                取消申请
              </Button>
            )}
          </Space>
        </Col>
      </Row>
    </Card>
  );
};

// ============ 页面组件 ============

const ProfileLeavePage: React.FC = () => {
  const [submitModalOpen, setSubmitModalOpen] = useState(false);

  // ============ 数据加载：使用 useState + useEffect 替代 useRequest ============

  const [balance, setBalance] = useState<LeaveBalanceVO>();
  const [leaveList, setLeaveList] = useState<LeaveVO[]>([]);
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [balanceRes, leaveRes] = await Promise.all([
        getLeaveBalance(),
        getLeaveList(),
      ]);
      console.log('[我的请假] balance raw:', balanceRes);
      console.log('[我的请假] leaveList raw:', leaveRes);
      setBalance(balanceRes);
      setLeaveList(leaveRes || []);
    } catch (err) {
      console.error('[我的请假] 加载失败:', err);
      message.error('数据加载失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // 过滤：不展示草稿(0)，已取消(4)仍展示
  const displayLeaves = useMemo(() => {
    return leaveList.filter((l) => l.approvalStatus !== 0);
  }, [leaveList]);

  // ============ 提交请假 ============

  const handleSubmitLeave = async (values: any) => {
    const startTime = values.dateRange[0];
    const endTime = values.dateRange[1];
    const totalDays = endTime.diff(startTime, 'day') + 1;

    const payload: LeaveRequestDTO = {
      leaveType: values.leaveType,
      startTime: startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: endTime.format('YYYY-MM-DD HH:mm:ss'),
      totalDays,
      leaveReason: values.leaveReason,
    };
    await createLeave(payload);
    message.success('请假申请已提交');
    setSubmitModalOpen(false);
    loadData();
  };

  // ============ 取消请假 ============

  const handleCancelLeave = async (id: number) => {
    try {
      await cancelLeave(id);
      message.success('请假已取消');
      loadData();
    } catch {
      // 静默处理
    }
  };

  // ============ 渲染 ============

  return (
    <PageContainer>
      {/* 顶部标题与操作 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 20 }}>
        <Col>
          <Title level={4} style={{ margin: 0 }}>
            我的请假
          </Title>
          <Text type="secondary">管理请假申请与审批进度</Text>
        </Col>
        <Col>
          <Button type="primary" onClick={() => setSubmitModalOpen(true)}>
            + 申请请假
          </Button>
        </Col>
      </Row>

      {/* 假期余额概览（3 卡片） */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {BALANCE_CARD_CONFIG.map((cfg) => (
          <BalanceCard key={cfg.key} config={cfg} balance={balance ?? {}} loading={loading} />
        ))}
      </Row>

      {/* 申请记录列表 */}
      <div style={{ marginBottom: 12 }}>
        <Title level={5} style={{ margin: 0 }}>
          申请记录
        </Title>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Spin />
        </div>
      ) : displayLeaves.length === 0 ? (
        <Card bordered={false} style={{ borderRadius: 8 }}>
          <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
            <div style={{ fontSize: 48, marginBottom: 12 }}>📋</div>
            <Text type="secondary">暂无请假记录</Text>
          </div>
        </Card>
      ) : (
        displayLeaves.map((record) => (
          <LeaveRecordCard key={record.id} record={record} onCancel={handleCancelLeave} />
        ))
      )}

      {/* 申请请假弹窗 */}
      <LeaveModal
        open={submitModalOpen}
        onClose={() => setSubmitModalOpen(false)}
        onSubmit={handleSubmitLeave}
      />
    </PageContainer>
  );
};

export default ProfileLeavePage;

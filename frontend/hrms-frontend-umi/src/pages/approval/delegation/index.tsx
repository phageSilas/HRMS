/**
 * 审批中心 - 委托审批页面
 *
 * 核心功能：
 * - 规则说明提示：一次性展示委托审批的业务规则
 * - 新增委托表单：选择被委托人 + 日期范围 + 委托原因
 * - 委托记录列表：展示全部委托，支持取消生效中的委托
 *
 * 数据流：
 * 初始化时调用 getMyDelegations 加载委托列表，
 * 新增委托提交调用 createDelegation，取消调用 cancelDelegation。
 * 每次操作成功后自动刷新列表。
 *
 * @module ApprovalDelegation
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Form,
  Select,
  DatePicker,
  Input,
  Button,
  Spin,
  Alert,
  Tag,
  Modal,
  message,
  Row,
  Col,
  Space,
  Typography,
} from 'antd';
import {
  UserOutlined,
  CheckCircleFilled,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import type { Delegation, DelegationCreateData } from '@/services/approval';
import {
  getMyDelegations,
  createDelegation,
  cancelDelegation,
} from '@/services/approval';
import { useEmployeeSearch } from '@/hooks/useEmployeeSearch';
import { getErrorMessage } from '@/utils/error';

const { Text } = Typography;

// ============ 常量定义 ============

/** 委托状态 → Tag 颜色 + 文本映射 */
const DELEGATION_STATUS_MAP: Record<string, { color: string; text: string }> = {
  active: { color: 'green', text: '生效中' },
  expired: { color: 'default', text: '已过期' },
  cancelled: { color: 'red', text: '已取消' },
};

// ============ 页面组件 ============

/** 委托审批主页面 */
const DelegationPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [delegations, setDelegations] = useState<Delegation[]>([]);
  const { options: employeeOptions, searching, search: handleEmployeeSearch } = useEmployeeSearch();

  // ============ 数据加载 ============

  /**
   * 获取委托数据
   *
   * 从 API 加载当前用户的所有委托记录（含已过期和已取消的）。
   */
  const fetchDelegations = useCallback(async () => {
    setLoading(true);
    try {
      const result = await getMyDelegations();
      setDelegations(result.records || []);
    } catch {
      message.error('获取委托信息失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDelegations();
  }, [fetchDelegations]);

  // ============ 事件处理 ============

  /**
   * 取消委托
   *
   * 弹出二次确认对话框，确认后调用取消接口并刷新列表。
   */
  const handleCancel = (id: number) => {
    Modal.confirm({
      title: '确认取消委托',
      content: '取消委托后，该委托将立即失效，确定要取消吗？',
      okText: '确认取消',
      cancelText: '暂不取消',
      onOk: async () => {
        try {
          await cancelDelegation(id);
          message.success('委托已取消');
          fetchDelegations();
        } catch {
          message.error('取消委托失败');
        }
      },
    });
  };

  /**
   * 提交新建委托
   *
   * 校验表单 → 构建 DelegationCreateData → 调用 createDelegation，
   * 成功后重置表单并刷新列表。
   * 表单校验失败（Ant Design 校验错误）时不处理。
   */
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const data: DelegationCreateData = {
        delegateeId: values.delegateeId,
        // 委托开始日期取当日 00:00:00，结束日期取 23:59:59 以覆盖全天
        startTime: values.dateRange[0].format('YYYY-MM-DD') + ' 00:00:00',
        endTime: values.dateRange[1].format('YYYY-MM-DD') + ' 23:59:59',
        reason: values.reason,
      };
      await createDelegation(data);
      message.success('委托创建成功');
      form.resetFields();
      fetchDelegations();
    } catch (err: unknown) {
      // 表单校验不通过时不处理
      if (err && typeof err === 'object' && 'errorFields' in err) return;
      message.error(getErrorMessage(err, '创建委托失败'));
    } finally {
      setSubmitting(false);
    }
  };

  /** 重置表单 */
  const handleReset = () => {
    form.resetFields();
  };

  // ============ 工具方法 ============

  /** 格式化时间范围为展示字符串：yyyy-MM-dd 至 yyyy-MM-dd */
  const formatDateRange = (startTime: string, endTime: string): string => {
    const fmt = 'YYYY-MM-DD';
    const start = dayjs(startTime).format(fmt);
    const end = dayjs(endTime).format(fmt);
    return `${start} 至 ${end}`;
  };

  /** 当前生效中的委托列表 */
  const activeDelegations = delegations.filter((d) => d.status === 'active');

  // ============ 渲染：规则说明提示框 ============

  const renderRulesAlert = () => (
    <Alert
      type="info"
      showIcon
      style={{
        borderRadius: 8,
        padding: '12px 20px',
        marginBottom: 16,
      }}
      message={
        <div>
          <Text strong style={{ fontSize: 14, marginBottom: 8, display: 'block' }}>
            委托审批规则说明
          </Text>
          <ul style={{ margin: 0, paddingLeft: 20, lineHeight: 2, fontSize: 13 }}>
            <li>委托期间产生的审批任务将自动转给被委托人处理</li>
            <li>被委托人审批时，系统将记录&ldquo;XXX 代 YYY 审批&rdquo;</li>
            <li>委托人可随时取消委托，取消后新任务不再转交</li>
            <li>同一时间只能有一个有效委托</li>
          </ul>
        </div>
      }
    />
  );

  // ============ 渲染：新增委托表单 ============

  const renderDelegationForm = () => (
    <Card
      title="新增委托"
      style={{ marginBottom: 16, borderRadius: 8 }}
      size="small"
    >
      <Form
        form={form}
        layout="vertical"
        style={{ maxWidth: 600 }}
      >
        {/* 被委托人：支持搜索的员工选择器 */}
        <Form.Item
          name="delegateeId"
          label={
            <span>
              被委托人 <Text type="danger">*</Text>
            </span>
          }
          rules={[{ required: true, message: '请选择被委托人' }]}
        >
          <Select
            showSearch
            placeholder="请选择被委托人"
            filterOption={false}
            notFoundContent={searching ? '搜索中...' : '请输入姓名或工号搜索'}
            loading={searching}
            onSearch={handleEmployeeSearch}
            labelInValue={false}
            options={employeeOptions.map((emp) => ({
              label: `${emp.employeeName}（${emp.employeeNo}）- ${emp.deptName || ''}${emp.postName ? '/' + emp.postName : ''}`,
              value: emp.id,
            }))}
          />
        </Form.Item>

        {/* 日期范围：起止日期并排显示 */}
        <Form.Item
          label={
            <span>
              日期范围 <Text type="danger">*</Text>
            </span>
          }
          required
          style={{ marginBottom: 0 }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name={['dateRange', 0]}
                rules={[{ required: true, message: '请选择开始日期' }]}
                style={{ marginBottom: 0 }}
              >
                <DatePicker
                  style={{ width: '100%' }}
                  format="YYYY/MM/DD"
                  placeholder="开始日期"
                  // 禁止选择今天之前的日期
                  disabledDate={(current) =>
                    current && current.isBefore(dayjs().startOf('day'))
                  }
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name={['dateRange', 1]}
                rules={[
                  { required: true, message: '请选择结束日期' },
                  // 自定义校验：结束日期不能早于开始日期
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      const range = getFieldValue('dateRange');
                      if (!value || !range || !range[0]) return Promise.resolve();
                      if (value.isAfter(range[0]) || value.isSame(range[0], 'day'))
                        return Promise.resolve();
                      return Promise.reject(
                        new Error('结束日期必须不早于开始日期'),
                      );
                    },
                  }),
                ]}
                style={{ marginBottom: 0 }}
              >
                <DatePicker
                  style={{ width: '100%' }}
                  format="YYYY/MM/DD"
                  placeholder="结束日期"
                />
              </Form.Item>
            </Col>
          </Row>
        </Form.Item>

        {/* 委托原因 */}
        <Form.Item name="reason" label="委托原因" style={{ marginTop: 24 }}>
          <Input placeholder="请输入委托原因（选填）" />
        </Form.Item>

        {/* 按钮区域 */}
        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={handleReset}>取消</Button>
            <Button
              type="primary"
              onClick={handleSubmit}
              loading={submitting}
            >
              确认添加
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );

  // ============ 渲染：委托列表项 ============

  /** 渲染单个委托记录项 */
  const renderDelegationItem = (record: Delegation) => {
    const statusConfig = DELEGATION_STATUS_MAP[record.status] || {
      color: 'default',
      text: record.status,
    };

    return (
      <div
        key={record.id}
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          padding: '16px 0',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        {/* 左侧头像 */}
        <div
          style={{
            width: 40,
            height: 40,
            borderRadius: '50%',
            backgroundColor: '#faad14',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            marginRight: 12,
          }}
        >
          <UserOutlined style={{ color: '#fff', fontSize: 20 }} />
        </div>

        {/* 中间信息 */}
        <div style={{ flex: 1, minWidth: 0 }}>
          {/* 第一行：姓名 + 职位标签 + 状态标签 */}
          <div style={{ marginBottom: 4 }}>
            <Text strong style={{ fontSize: 14, marginRight: 8 }}>
              {record.delegateeName}
            </Text>
            {record.position && (
              <Tag color="default" style={{ marginRight: 6 }}>
                {record.position}
              </Tag>
            )}
            <Tag color={statusConfig.color}>{statusConfig.text}</Tag>
          </div>

          {/* 第二行：时间范围 + 委托原因 */}
          <div>
            <Text type="secondary" style={{ fontSize: 12, marginRight: 12 }}>
              {formatDateRange(record.startTime, record.endTime)}
            </Text>
            {record.reason && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {record.reason}
              </Text>
            )}
          </div>
        </div>

        {/* 右侧操作（仅生效中可见取消按钮） */}
        <div style={{ flexShrink: 0, marginLeft: 12 }}>
          {record.status === 'active' && (
            <Button
              type="link"
              danger
              size="small"
              onClick={() => handleCancel(record.id)}
            >
              取消委托
            </Button>
          )}
        </div>
      </div>
    );
  };

  // ============ 渲染：委托列表卡片 ============

  const renderDelegationList = () => (
    <Card
      style={{ borderRadius: 8 }}
      size="small"
    >
      {/* 自定义标题栏 */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 0,
        }}
      >
        <Space>
          <CheckCircleFilled style={{ color: '#52c41a', fontSize: 16 }} />
          <Text strong style={{ fontSize: 14 }}>
            委托记录
          </Text>
        </Space>
        <Text type="secondary" style={{ fontSize: 13 }}>
          {activeDelegations.length}条
        </Text>
      </div>

      {/* 列表内容 */}
      <Spin spinning={loading}>
        {delegations.length > 0 ? (
          <div style={{ marginTop: 8 }}>
            {delegations.map((record) => renderDelegationItem(record))}
          </div>
        ) : (
          !loading && (
            <div
              style={{
                textAlign: 'center',
                padding: '40px 0',
                color: '#999',
                marginTop: 8,
              }}
            >
              暂无委托记录
            </div>
          )
        )}
      </Spin>
    </Card>
  );

  // ============ 主渲染 ============

  return (
    <div style={{ padding: 0 }}>
      {renderRulesAlert()}
      {renderDelegationForm()}
      {renderDelegationList()}
    </div>
  );
};

export default DelegationPage;

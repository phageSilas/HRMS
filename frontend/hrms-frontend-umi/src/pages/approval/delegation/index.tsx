/**
 * 委托审批设置页面
 *
 * 功能：查看当前生效委托、创建新委托、查看历史委托记录、取消委托
 */
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
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
} from 'antd';
import dayjs from 'dayjs';
import type { Delegation, DelegationCreateData } from '@/services/approval';
import {
  getMyDelegations,
  createDelegation,
  cancelDelegation,
} from '@/services/approval';
import { getEmployeeList } from '@/services/employee';
import type { EmployeeBrief } from '@/services/employee';

// ============ 常量定义 ============

/** 委托状态标签颜色映射 */
const DELEGATION_STATUS_MAP: Record<string, { color: string; text: string }> = {
  active: { color: 'green', text: '生效中' },
  expired: { color: 'default', text: '已过期' },
  cancelled: { color: 'red', text: '已取消' },
};

// ============ 页面组件 ============

const DelegationPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [employeeOptions, setEmployeeOptions] = useState<EmployeeBrief[]>([]);
  const [searching, setSearching] = useState<boolean>(false);
  const [activeDelegation, setActiveDelegation] = useState<Delegation | null>(
    null,
  );
  const [delegations, setDelegations] = useState<Delegation[]>([]);

  // ============ 数据加载 ============

  /** 获取委托数据 */
  const fetchDelegations = useCallback(async () => {
    setLoading(true);
    try {
      const result = await getMyDelegations();
      setActiveDelegation(result.activeDelegation);
      setDelegations(result.records);
    } catch {
      message.error('获取委托信息失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDelegations();
  }, [fetchDelegations]);

  // ============ 员工搜索 ============

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

  // ============ 事件处理 ============

  /** 取消委托（二次确认） */
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

  /** 提交新建委托 */
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const data: DelegationCreateData = {
        delegateeId: values.delegateeId,
        startTime: values.startTime.format('YYYY-MM-DD HH:mm:ss'),
        endTime: values.endTime.format('YYYY-MM-DD HH:mm:ss'),
        reason: values.reason,
      };
      await createDelegation(data);
      message.success('委托创建成功');
      form.resetFields();
      fetchDelegations();
    } catch (error: any) {
      // 表单校验不通过时不处理
      if (error?.errorFields) return;
      message.error(error?.message || '创建委托失败');
    } finally {
      setSubmitting(false);
    }
  };

  // ============ 表格列定义 ============

  const columns: ProColumns<Delegation>[] = [
    { title: '被委托人', dataIndex: 'delegateeName', width: 120 },
    {
      title: '生效时间',
      dataIndex: 'startTime',
      width: 170,
      valueType: 'dateTime',
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 170,
      valueType: 'dateTime',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (_, record) => (
        <Tag
          color={DELEGATION_STATUS_MAP[record.status]?.color || 'default'}
        >
          {DELEGATION_STATUS_MAP[record.status]?.text || record.status}
        </Tag>
      ),
    },
    {
      title: '操作',
      width: 100,
      render: (_, record) =>
        record.status === 'active' && (
          <a onClick={() => handleCancel(record.id)}>取消</a>
        ),
    },
  ];

  // ============ 渲染 ============

  return (
    <PageContainer>
      <Spin spinning={loading}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* 当前生效委托提示卡片 */}
          {activeDelegation && (
            <Alert
              type="info"
              showIcon
              message={
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <span>
                    当前已委托 <strong>{activeDelegation.delegateeName}</strong>
                    {activeDelegation.startTime &&
                      `（生效时间：${activeDelegation.startTime}`}
                    {activeDelegation.endTime &&
                      ` 至 ${activeDelegation.endTime}）`}
                  </span>
                  <Button
                    size="small"
                    danger
                    onClick={() => handleCancel(activeDelegation.id)}
                  >
                    取消委托
                  </Button>
                </div>
              }
            />
          )}

          {/* 新建委托表单 */}
          <Card title="新建委托">
            <Form form={form} layout="vertical" style={{ maxWidth: 500 }}>
              <Form.Item
                name="delegateeId"
                label="被委托人"
                rules={[{ required: true, message: '请选择被委托人' }]}
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
              </Form.Item>
              <Form.Item
                name="startTime"
                label="生效时间"
                rules={[{ required: true, message: '请选择生效时间' }]}
              >
                <DatePicker
                  showTime
                  style={{ width: '100%' }}
                  disabledDate={(current) =>
                    current && current.isBefore(dayjs().startOf('day'))
                  }
                  placeholder="请选择生效时间"
                />
              </Form.Item>
              <Form.Item
                name="endTime"
                label="结束时间"
                rules={[
                  { required: true, message: '请选择结束时间' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || !getFieldValue('startTime'))
                        return Promise.resolve();
                      if (value.isAfter(getFieldValue('startTime')))
                        return Promise.resolve();
                      return Promise.reject(
                        new Error('结束时间必须晚于生效时间'),
                      );
                    },
                  }),
                ]}
              >
                <DatePicker
                  showTime
                  style={{ width: '100%' }}
                  placeholder="请选择结束时间"
                />
              </Form.Item>
              <Form.Item name="reason" label="委托原因">
                <Input.TextArea rows={3} placeholder="可选填写委托原因" />
              </Form.Item>
              <Form.Item>
                <Button
                  type="primary"
                  onClick={handleSubmit}
                  loading={submitting}
                >
                  提交委托
                </Button>
              </Form.Item>
            </Form>
          </Card>

          {/* 委托记录表格 */}
          <Card title="委托记录">
            <ProTable<Delegation>
              columns={columns}
              dataSource={delegations}
              rowKey="id"
              search={false}
              pagination={{
                showSizeChanger: true,
                showTotal: (total: number) => `共 ${total} 条`,
              }}
              toolBarRender={false}
            />
          </Card>
        </div>
      </Spin>
    </PageContainer>
  );
};

export default DelegationPage;

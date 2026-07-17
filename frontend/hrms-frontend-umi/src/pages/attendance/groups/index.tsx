import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type {
  AttendanceGroup,
  AttendanceGroupRequest,
} from '@/services/attendance';
import {
  createAttendanceGroup,
  getAttendanceGroups,
  updateAttendanceGroup,
} from '@/services/attendance';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  EditOutlined,
  FileSearchOutlined,
  PlusOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history, useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Spin,
  Switch,
  Tag,
  TimePicker,
  Typography,
  message,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

const { Text, Title } = Typography;

type GroupFormValues = Omit<
  AttendanceGroupRequest,
  | 'clockInTime'
  | 'clockOutTime'
  | 'restStartTime'
  | 'restEndTime'
  | 'flexibleStartTime'
  | 'flexibleEndTime'
  | 'locationRange'
> & {
  clockInTime: dayjs.Dayjs;
  clockOutTime: dayjs.Dayjs;
  restStartTime?: dayjs.Dayjs;
  restEndTime?: dayjs.Dayjs;
  flexibleStartTime?: dayjs.Dayjs;
  flexibleEndTime?: dayjs.Dayjs;
  locationLatitude?: number;
  locationLongitude?: number;
  locationRadius?: number;
  locationAddress?: string;
  enabled?: boolean;
};

type AttendanceGroupPageLike =
  | {
      records?: AttendanceGroup[];
      data?: { records?: AttendanceGroup[] } | AttendanceGroup[];
    }
  | AttendanceGroup[]
  | undefined;

const shiftTypeMap: Record<string, { label: string; color: string }> = {
  FIXED: { label: '固定班', color: 'blue' },
  FLEXIBLE: { label: '弹性班', color: 'purple' },
  SCHEDULED: { label: '排班制', color: 'orange' },
};

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

function formatBackendTime(value?: string | number[]) {
  if (!value) return '--:--';
  if (Array.isArray(value)) {
    const [hour = 0, minute = 0] = value;
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
  }
  return value.slice(0, 5);
}

function toPickerTime(value?: string | number[]) {
  const text = formatBackendTime(value);
  return text === '--:--' ? undefined : dayjs(`2026-01-01 ${text}:00`);
}

function toRequestTime(value?: dayjs.Dayjs) {
  return value ? value.format('HH:mm:ss') : undefined;
}

function getShiftMeta(type?: string) {
  return shiftTypeMap[(type || 'FIXED').toUpperCase()] || {
    label: type || '未配置',
    color: 'default',
  };
}

function normalizeGroups(pageData: AttendanceGroupPageLike) {
  if (Array.isArray(pageData)) return pageData;
  if (!pageData) return [];
  if (Array.isArray(pageData.records)) return pageData.records;
  if (Array.isArray(pageData.data)) return pageData.data;
  if (
    pageData.data &&
    !Array.isArray(pageData.data) &&
    Array.isArray(pageData.data.records)
  ) {
    return pageData.data.records;
  }
  return [];
}

const AttendanceGroupsPage: React.FC = () => {
  const [form] = Form.useForm<GroupFormValues>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState<AttendanceGroup>();
  const [query] = useState({ pageNum: 1, pageSize: 20 });

  const { data, loading, refresh } = useRequest(
    () => getAttendanceGroups(query),
    { refreshDeps: [query] },
  );

  usePageAutoRefresh(() => {
    refresh();
  });

  const groups = useMemo(() => {
    return normalizeGroups(data as AttendanceGroupPageLike);
  }, [data]);

  const activeCount = useMemo(
    () => groups.filter((item) => item.status !== 0).length,
    [groups],
  );

  useEffect(() => {
    if (!drawerOpen) return;

    if (!editingGroup) {
      form.setFieldsValue({
        shiftType: 'FIXED',
        clockInTime: dayjs('2026-01-01 09:00:00'),
        clockOutTime: dayjs('2026-01-01 18:00:00'),
        lateThreshold: 15,
        earlyLeaveThreshold: 15,
        maxCorrectionCount: 2,
        enabled: true,
      });
      return;
    }

    form.setFieldsValue({
      groupName: editingGroup.groupName,
      shiftType: (editingGroup.shiftType || 'FIXED').toUpperCase(),
      clockInTime: toPickerTime(editingGroup.workStartTime),
      clockOutTime: toPickerTime(editingGroup.workEndTime),
      lateThreshold: editingGroup.lateThresholdMinutes,
      earlyLeaveThreshold: editingGroup.earlyLeaveThresholdMinutes,
      maxCorrectionCount: editingGroup.monthlyCorrectionLimit,
      enabled: editingGroup.status !== 0,
    });
  }, [drawerOpen, editingGroup, form]);

  const openCreateDrawer = () => {
    setEditingGroup(undefined);
    form.resetFields();
    setDrawerOpen(true);
  };

  const openEditDrawer = (group: AttendanceGroup) => {
    setEditingGroup(group);
    form.resetFields();
    setDrawerOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: AttendanceGroupRequest = {
      groupName: values.groupName,
      shiftType: values.shiftType,
      clockInTime: toRequestTime(values.clockInTime) || '09:00:00',
      clockOutTime: toRequestTime(values.clockOutTime) || '18:00:00',
      restStartTime: toRequestTime(values.restStartTime),
      restEndTime: toRequestTime(values.restEndTime),
      flexibleStartTime: toRequestTime(values.flexibleStartTime),
      flexibleEndTime: toRequestTime(values.flexibleEndTime),
      lateThreshold: values.lateThreshold,
      earlyLeaveThreshold: values.earlyLeaveThreshold,
      maxCorrectionCount: values.maxCorrectionCount,
      ipWhitelist: values.ipWhitelist,
      status: values.enabled === false ? 0 : 1,
      locationRange: {
        latitude: values.locationLatitude,
        longitude: values.locationLongitude,
        radius: values.locationRadius,
        address: values.locationAddress,
      },
    };

    if (editingGroup) {
      await updateAttendanceGroup(editingGroup.id, payload);
      message.success('考勤组已更新');
    } else {
      await createAttendanceGroup(payload);
      message.success('考勤组已新增');
    }

    setDrawerOpen(false);
    refresh();
  };

  return (
    <PageContainer title={false} className={styles.groupsPage}>
      <div className={styles.pageHeader}>
        <div>
          <Title level={3}>考勤规则配置</Title>
          <Text type="secondary">管理考勤组、工作日及打卡规则</Text>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={openCreateDrawer}
        >
          新增考勤组
        </Button>
      </div>

      <Spin spinning={loading}>
        {groups.length > 0 ? (
          <Row gutter={[16, 16]} className={styles.groupGrid}>
            {groups.map((group) => {
              const shiftMeta = getShiftMeta(group.shiftType);
              return (
                <Col xs={24} md={12} xl={8} key={group.id}>
                  <Card
                    bordered={false}
                    className={styles.groupCard}
                    extra={
                      <Button
                        type="text"
                        icon={<EditOutlined />}
                        onClick={() => openEditDrawer(group)}
                      />
                    }
                  >
                    <Space direction="vertical" size={10}>
                      <Space>
                        <Text strong className={styles.groupName}>
                          {group.groupName}
                        </Text>
                        <Tag color={shiftMeta.color}>{shiftMeta.label}</Tag>
                        <Tag color={group.status === 0 ? 'default' : 'success'}>
                          {group.status === 0 ? '停用' : '启用'}
                        </Tag>
                      </Space>
                      <div className={styles.groupMeta}>
                        <ClockCircleOutlined />
                        {formatBackendTime(group.workStartTime)} -{' '}
                        {formatBackendTime(group.workEndTime)}
                      </div>
                      <div className={styles.groupMeta}>
                        <TeamOutlined />
                        成员范围由后端考勤组规则解析
                      </div>
                      <div className={styles.groupFooter}>
                        <span>补卡上限 {group.monthlyCorrectionLimit ?? 0} 次/月</span>
                        <span>
                          迟到阈值 {group.lateThresholdMinutes ?? 0}min
                        </span>
                      </div>
                      <div className={styles.groupActions}>
                        <Button
                          type="link"
                          icon={<FileSearchOutlined />}
                          onClick={() =>
                            history.push(`/attendance/record?groupId=${group.id}`)
                          }
                        >
                          查看记录
                        </Button>
                      </div>
                    </Space>
                  </Card>
                </Col>
              );
            })}
          </Row>
        ) : (
          <Card bordered={false} className={styles.emptyCard}>
            <Empty
              description="暂无考勤组，请先新增考勤规则"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          </Card>
        )}
      </Spin>

      <Row gutter={[16, 16]} className={styles.rulePanels}>
        <Col xs={24} lg={12}>
          <Card bordered={false} className={styles.ruleCard}>
            <Title level={5}>工作日设置</Title>
            <Space wrap className={styles.weekLine}>
              {weekDays.map((day, index) => (
                <Tag
                  key={day}
                  color={index < 5 ? 'blue' : 'default'}
                  className={styles.weekTag}
                >
                  {day}
                </Tag>
              ))}
            </Space>
            <div className={styles.tipBox}>
              法定节假日需提前在系统中配置，配置后将自动排除在工作日之外。
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card bordered={false} className={styles.ruleCard}>
            <Title level={5}>打卡规则说明</Title>
            <div className={styles.ruleList}>
              <span>上班打卡时间 &lt; 规定时间</span>
              <Tag color="success">正常</Tag>
              <span>规定时间 &lt; 打卡时间 &lt;= 规定时间+阈值</span>
              <Tag color="warning">迟到</Tag>
              <span>上班打卡时间 &gt; 规定时间+阈值</span>
              <Tag color="error">旷工</Tag>
              <span>下班打卡时间 &gt;= 规定时间</span>
              <Tag color="success">正常</Tag>
              <span>下班打卡时间 &lt; 规定时间</span>
              <Tag color="warning">早退</Tag>
              <span>当日无打卡记录</span>
              <Tag>缺勤</Tag>
            </div>
            <Text type="secondary">
              当前已启用 {activeCount} 个考勤组；补卡申请默认每月最多 2 次，可在考勤组内单独配置。
            </Text>
          </Card>
        </Col>
      </Row>

      <Drawer
        title={editingGroup ? '编辑考勤组' : '新增考勤组'}
        open={drawerOpen}
        width={560}
        destroyOnClose
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              保存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item
            label="考勤组名称"
            name="groupName"
            rules={[{ required: true, message: '请输入考勤组名称' }]}
          >
            <Input placeholder="如：标准工时组" />
          </Form.Item>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item
                label="班次类型"
                name="shiftType"
                rules={[{ required: true, message: '请选择班次类型' }]}
              >
                <Select
                  options={[
                    { label: '固定班', value: 'FIXED' },
                    { label: '弹性班', value: 'FLEXIBLE' },
                    { label: '排班制', value: 'SCHEDULED' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="启用状态" name="enabled" valuePropName="checked">
                <Switch checkedChildren="启用" unCheckedChildren="停用" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item
                label="上班时间"
                name="clockInTime"
                rules={[{ required: true, message: '请选择上班时间' }]}
              >
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="下班时间"
                name="clockOutTime"
                rules={[{ required: true, message: '请选择下班时间' }]}
              >
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item label="午休开始" name="restStartTime">
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="午休结束" name="restEndTime">
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item label="弹性最早打卡" name="flexibleStartTime">
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="弹性最晚打卡" name="flexibleEndTime">
                <TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={8}>
              <Form.Item label="迟到阈值" name="lateThreshold">
                <InputNumber min={0} addonAfter="分钟" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="早退阈值" name="earlyLeaveThreshold">
                <InputNumber min={0} addonAfter="分钟" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="补卡上限" name="maxCorrectionCount">
                <InputNumber min={0} addonAfter="次/月" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="IP 白名单" name="ipWhitelist">
            <Input placeholder="多个 IP 用英文逗号分隔" />
          </Form.Item>
          <Card size="small" className={styles.locationFormCard}>
            <Space>
              <CalendarOutlined />
              <Text strong>打卡范围</Text>
            </Space>
            <Row gutter={12} style={{ marginTop: 12 }}>
              <Col span={8}>
                <Form.Item label="纬度" name="locationLatitude">
                  <InputNumber precision={6} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="经度" name="locationLongitude">
                  <InputNumber precision={6} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="半径" name="locationRadius">
                  <InputNumber min={0} addonAfter="米" style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item label="地址说明" name="locationAddress">
              <Input placeholder="如：杭州总部 A 座" />
            </Form.Item>
          </Card>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default AttendanceGroupsPage;

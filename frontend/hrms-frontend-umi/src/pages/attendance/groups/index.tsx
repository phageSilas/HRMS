import { usePageAutoRefresh } from '@/hooks/usePageAutoRefresh';
import type {
  AttendanceCalendarConfig,
  AttendanceGroup,
  AttendanceGroupRequest,
  AttendanceGroupScopeType,
} from '@/services/attendance';
import {
  createAttendanceGroup,
  deleteAttendanceGroup,
  getAttendanceCalendarConfig,
  getAttendanceGroups,
  updateAttendanceCalendarConfig,
  updateAttendanceGroup,
} from '@/services/attendance';
import type { EmployeeBrief } from '@/services/employee';
import { getEmployeeList } from '@/services/employee';
import type { DeptTreeNode, PostItem } from '@/services/organization';
import { getDepartmentTree, getPostList } from '@/services/organization';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  EditOutlined,
  FileSearchOutlined,
  PlusOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import {
  Calendar as AntCalendar,
  Button,
  Card,
  Col,
  DatePicker,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Radio,
  Row,
  Select,
  Space,
  Spin,
  Switch,
  Tag,
  TimePicker,
  TreeSelect,
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
  | 'memberRange'
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
  scopeType?: AttendanceGroupScopeType;
  deptIds?: number[];
  postId?: number;
  employeeDeptId?: number;
  employeeIds?: number[];
};

type TreeSelectNode = {
  title: string;
  value: number;
  key: number;
  children?: TreeSelectNode[];
};

const shiftTypeMap: Record<string, { label: string; color: string }> = {
  FIXED: { label: '固定班', color: 'blue' },
  FLEXIBLE: { label: '弹性班', color: 'purple' },
  SCHEDULED: { label: '排班制', color: 'orange' },
};

const scopeTypeText: Record<AttendanceGroupScopeType, string> = {
  DEPT: '部门',
  POST: '职位',
  EMPLOYEE: '指定员工',
};

const weekdayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
];

const defaultCalendarConfig: AttendanceCalendarConfig = {
  year: dayjs().year(),
  workdays: [1, 2, 3, 4, 5],
  holidayDates: [],
};

/** 格式化后端返回的班次时间，统一为 HH:mm 展示。 */
function formatBackendTime(value?: string | number[]) {
  if (!value) return '--:--';
  if (Array.isArray(value)) {
    const [hour = 0, minute = 0] = value;
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(
      2,
      '0',
    )}`;
  }
  return value.slice(0, 5);
}

/** 将后端时间转换为 TimePicker 值，内部调用 `formatBackendTime` 统一解析格式。 */
function toPickerTime(value?: string | number[]) {
  const text = formatBackendTime(value);
  return text === '--:--' ? undefined : dayjs(`2026-01-01 ${text}:00`);
}

/** 将 TimePicker 值转换为接口需要的字符串时间。 */
function toRequestTime(value?: dayjs.Dayjs) {
  return value ? value.format('HH:mm:ss') : undefined;
}

/** 规范化节假日日期值，统一兼容数组、紧凑字符串和标准日期字符串。 */
function normalizeHolidayDateValue(value: string | number[]) {
  if (Array.isArray(value)) {
    const [year, month, day] = value;
    if (!year || !month || !day) {
      return '';
    }
    return `${String(year).padStart(4, '0')}-${String(month).padStart(
      2,
      '0',
    )}-${String(day).padStart(2, '0')}`;
  }

  if (/^\d{8}$/.test(value)) {
    return `${value.slice(0, 4)}-${value.slice(4, 6)}-${value.slice(6, 8)}`;
  }

  const parsed = dayjs(value, ['YYYY-MM-DD', 'YYYY/M/D', 'YYYY/M/DD', 'YYYY/MM/D', 'YYYY/MM/DD']);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : value;
}

/** 生成节假日展示文案，内部调用 `normalizeHolidayDateValue` 统一格式。 */
function formatHolidayDateLabel(value: string | number[]) {
  const normalized = normalizeHolidayDateValue(value);
  const parsed = dayjs(normalized, 'YYYY-MM-DD');
  return parsed.isValid() ? parsed.format('YYYY/MM/DD') : normalized;
}

/** 解析定位范围 JSON 字符串，供编辑考勤组时回填定位字段。 */
function parseLocationRange(value?: string) {
  if (!value) {
    return {};
  }

  try {
    const parsed = JSON.parse(value) as {
      latitude?: number | string;
      longitude?: number | string;
      radius?: number | string;
      address?: string;
    };

    const latitude =
      parsed.latitude == null || parsed.latitude === ''
        ? undefined
        : Number(parsed.latitude);
    const longitude =
      parsed.longitude == null || parsed.longitude === ''
        ? undefined
        : Number(parsed.longitude);
    const radius =
      parsed.radius == null || parsed.radius === ''
        ? undefined
        : Number(parsed.radius);

    return {
      locationLatitude: Number.isFinite(latitude) ? latitude : undefined,
      locationLongitude: Number.isFinite(longitude) ? longitude : undefined,
      locationRadius: Number.isFinite(radius) ? radius : undefined,
      locationAddress: parsed.address || undefined,
    };
  } catch {
    return {};
  }
}

/** 根据班次类型获取标签样式和文案。 */
function getShiftMeta(type?: string) {
  return (
    shiftTypeMap[(type || 'FIXED').toUpperCase()] || {
      label: type || '未配置',
      color: 'default',
    }
  );
}

/** 将部门树转换为 TreeSelect 可用结构。 */
function toTreeSelectData(nodes?: DeptTreeNode[]): TreeSelectNode[] {
  return (nodes || []).map((node) => ({
    title: node.deptName,
    value: node.id,
    key: node.id,
    children: toTreeSelectData(node.children),
  }));
}

/** 构建成员范围请求体，供提交考勤组表单时复用。 */
function buildMemberRange(
  values: GroupFormValues,
): AttendanceGroupRequest['memberRange'] {
  const scopeType = values.scopeType || 'DEPT';
  if (scopeType === 'DEPT') {
    return {
      scopeType,
      deptIds: values.deptIds || [],
    };
  }
  if (scopeType === 'POST') {
    return {
      scopeType,
      postId: values.postId,
    };
  }
  return {
    scopeType,
    deptId: values.employeeDeptId,
    employeeIds: values.employeeIds || [],
  };
}

/**
 * 考勤组管理页面组件。
 * 负责考勤组维护、成员范围配置和工作日历规则配置。
 */
const AttendanceGroupsPage: React.FC = () => {
  const [form] = Form.useForm<GroupFormValues>();
  const scopeType = Form.useWatch('scopeType', form);
  const employeeDeptId = Form.useWatch('employeeDeptId', form);

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState<AttendanceGroup>();
  const [query] = useState({ pageNum: 1, pageSize: 20 });
  const [groups, setGroups] = useState<AttendanceGroup[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [departmentTree, setDepartmentTree] = useState<DeptTreeNode[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [postOptions, setPostOptions] = useState<PostItem[]>([]);
  const [postLoading, setPostLoading] = useState(false);
  const [employeeOptions, setEmployeeOptions] = useState<EmployeeBrief[]>([]);
  const [employeeLoading, setEmployeeLoading] = useState(false);
  const [calendarConfigYear, setCalendarConfigYear] = useState(dayjs().year());
  const [calendarConfigLoading, setCalendarConfigLoading] = useState(false);
  const [calendarConfigSaving, setCalendarConfigSaving] = useState(false);
  const [selectedWorkdays, setSelectedWorkdays] = useState<number[]>(
    defaultCalendarConfig.workdays,
  );
  const [selectedHolidayDates, setSelectedHolidayDates] = useState<string[]>(
    defaultCalendarConfig.holidayDates,
  );
  const [calendarPanelDate, setCalendarPanelDate] = useState(
    dayjs(`${dayjs().year()}-01-01`, 'YYYY-MM-DD'),
  );

  const departmentTreeData = useMemo(
    () => toTreeSelectData(departmentTree),
    [departmentTree],
  );
  const selectedHolidayDateSet = useMemo(
    () => new Set(selectedHolidayDates),
    [selectedHolidayDates],
  );

  /** 加载考勤组列表，供页面初始化和新增编辑删除后复用。 */
  const loadGroups = async () => {
    setLoading(true);
    try {
      const page = await getAttendanceGroups(query);
      const nextGroups = page.records ?? [];
      setGroups(nextGroups);
      return nextGroups;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '考勤组加载失败';
      message.error(messageText);
      return groups;
    } finally {
      setLoading(false);
    }
  };

  /** 加载考勤日历配置，供工作日配置抽屉切换年份和初始化时复用。 */
  const loadCalendarConfig = async (year = calendarConfigYear) => {
    setCalendarConfigLoading(true);
    try {
      const config = await getAttendanceCalendarConfig(year);
      setSelectedWorkdays(
        (config?.workdays?.length ? config.workdays : defaultCalendarConfig.workdays)
          .slice()
          .sort((left, right) => left - right),
      );
      setSelectedHolidayDates(
        (config?.holidayDates || [])
          .map((item) => normalizeHolidayDateValue(item))
          .filter((item) => !!item)
          .sort(),
      );
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '工作日和节假日配置加载失败';
      message.error(messageText);
    } finally {
      setCalendarConfigLoading(false);
    }
  };

  /** 加载部门树数据，供考勤组成员范围按部门选择时使用。 */
  const loadDepartmentTree = async () => {
    if (departmentTree.length > 0) {
      return;
    }
    setDepartmentLoading(true);
    try {
      setDepartmentTree(await getDepartmentTree());
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '部门数据加载失败';
      message.error(messageText);
    } finally {
      setDepartmentLoading(false);
    }
  };

  /** 加载岗位选项，供考勤组成员范围按岗位选择时使用。 */
  const loadPostOptions = async () => {
    if (postOptions.length > 0) {
      return;
    }
    setPostLoading(true);
    try {
      const page = await getPostList({ pageNum: 1, pageSize: 100 });
      setPostOptions(page.records || []);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '职位数据加载失败';
      message.error(messageText);
    } finally {
      setPostLoading(false);
    }
  };

  const searchEmployees = async (keyword = '', deptId = employeeDeptId) => {
    if (!deptId) {
      setEmployeeOptions([]);
      return;
    }
    setEmployeeLoading(true);
    try {
      const page = await getEmployeeList({
        keyword,
        deptIds: [deptId],
        pageNum: 1,
        pageSize: 20,
      });
      setEmployeeOptions(page.records || []);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '员工数据加载失败';
      message.error(messageText);
    } finally {
      setEmployeeLoading(false);
    }
  };

  useEffect(() => {
    void loadGroups();
  }, [query]);

  useEffect(() => {
    void loadCalendarConfig(calendarConfigYear);
  }, [calendarConfigYear]);

  usePageAutoRefresh(() => {
    void loadGroups();
    void loadCalendarConfig(calendarConfigYear);
  });

  useEffect(() => {
    if (!drawerOpen) {
      return;
    }
    void loadDepartmentTree();
    void loadPostOptions();
  }, [drawerOpen]);

  useEffect(() => {
    if (!drawerOpen || scopeType !== 'EMPLOYEE') {
      return;
    }
    void searchEmployees('', employeeDeptId);
  }, [employeeDeptId]);

  const activeCount = useMemo(
    () => groups.filter((item) => item.status !== 0).length,
    [groups],
  );

  const toggleWorkday = (value: number) => {
    setSelectedWorkdays((previous) => {
      if (previous.includes(value)) {
        if (previous.length === 1) {
          message.warning('至少保留一个工作日');
          return previous;
        }
        return previous.filter((item) => item !== value);
      }
      return [...previous, value].sort((left, right) => left - right);
    });
  };

  const toggleHolidayDate = (value: dayjs.Dayjs) => {
    const dateText = value.format('YYYY-MM-DD');
    if (value.year() !== calendarConfigYear) {
      return;
    }
    setSelectedHolidayDates((previous) => {
      if (previous.includes(dateText)) {
        return previous.filter((item) => item !== dateText);
      }
      return [...previous, dateText].sort();
    });
    setCalendarPanelDate(value);
  };

  /** 保存日历配置，内部调用 `loadCalendarConfig` 回刷最新规则。 */
  const handleSaveCalendarConfig = async () => {
    try {
      setCalendarConfigSaving(true);
      await updateAttendanceCalendarConfig({
        year: calendarConfigYear,
        workdays: selectedWorkdays,
        holidayDates: selectedHolidayDates,
      });
      message.success('工作日和节假日配置已保存');
      await loadCalendarConfig(calendarConfigYear);
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '工作日和节假日配置保存失败';
      message.error(messageText);
    } finally {
      setCalendarConfigSaving(false);
    }
  };

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
        scopeType: 'DEPT',
      });
      return;
    }

    const nextScopeType = (editingGroup.scopeType ||
      'DEPT') as AttendanceGroupScopeType;
    const locationRange = parseLocationRange(editingGroup.clockGpsScope);
    form.setFieldsValue({
      groupName: editingGroup.groupName,
      shiftType: (editingGroup.shiftType || 'FIXED').toUpperCase(),
      clockInTime: toPickerTime(editingGroup.workStartTime),
      clockOutTime: toPickerTime(editingGroup.workEndTime),
      lateThreshold: editingGroup.lateThresholdMinutes,
      earlyLeaveThreshold: editingGroup.earlyLeaveThresholdMinutes,
      maxCorrectionCount: editingGroup.monthlyCorrectionLimit,
      ipWhitelist: editingGroup.clockIpWhitelist,
      enabled: editingGroup.status !== 0,
      scopeType: nextScopeType,
      deptIds: editingGroup.deptIds || [],
      postId: editingGroup.postId,
      employeeDeptId: editingGroup.deptId,
      employeeIds: editingGroup.employeeIds || [],
      ...locationRange,
    });
    if (nextScopeType === 'EMPLOYEE') {
      void searchEmployees('', editingGroup.deptId);
    }
  }, [drawerOpen, editingGroup, form]);

  /** 打开新建考勤组抽屉，并重置表单和编辑上下文。 */
  const openCreateDrawer = () => {
    setEditingGroup(undefined);
    form.resetFields();
    setDrawerOpen(true);
  };

  /** 打开编辑考勤组抽屉，内部调用时间和定位解析方法回填表单。 */
  const openEditDrawer = (group: AttendanceGroup) => {
    setEditingGroup(group);
    form.resetFields();
    setDrawerOpen(true);
  };

  /** 关闭考勤组抽屉并清理编辑态。 */
  const closeDrawer = () => {
    setDrawerOpen(false);
    setEditingGroup(undefined);
    setDeleting(false);
    setSubmitting(false);
    setEmployeeOptions([]);
    form.resetFields();
  };

  /** 提交考勤组表单，内部调用 `buildMemberRange` 组装成员范围并刷新列表。 */
  const handleSubmit = async () => {
    try {
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
        memberRange: buildMemberRange(values),
      };

      setSubmitting(true);
      if (editingGroup) {
        await updateAttendanceGroup(editingGroup.id, payload);
        message.success('考勤组已更新');
      } else {
        await createAttendanceGroup(payload);
        message.success('考勤组已新增');
      }

      closeDrawer();
      await loadGroups();
    } catch (error) {
      if (error && typeof error === 'object' && 'errorFields' in error) {
        return;
      }
      const messageText =
        error instanceof Error ? error.message : '考勤组保存失败';
      message.error(messageText);
    } finally {
      setSubmitting(false);
    }
  };

  /** 删除当前选中的考勤组，成功后调用 `loadGroups` 刷新列表。 */
  const handleDeleteGroup = () => {
    if (!editingGroup?.id) {
      return;
    }

    Modal.confirm({
      title: '是否确认删除该考勤组？',
      content: '删除后不可恢复。',
      okText: '确认删除',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          setDeleting(true);
          await deleteAttendanceGroup(editingGroup.id);
          message.success('考勤组已删除');
          closeDrawer();
          await loadGroups();
        } catch (error) {
          const messageText =
            error instanceof Error ? error.message : '考勤组删除失败';
          message.error(messageText);
        } finally {
          setDeleting(false);
        }
      },
    });
  };

  return (
    <PageContainer title={false} className={styles.groupsPage}>
      <div className={styles.pageHeader}>
        <div>
          <Title level={3}>考勤规则配置</Title>
          <Text type="secondary">管理考勤组、适用范围、工作日及打卡规则</Text>
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
              const scopeText = group.scopeName || '暂未配置适用范围';
              const normalizedScopeType = group.scopeType as
                | AttendanceGroupScopeType
                | undefined;
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
                    <Space
                      direction="vertical"
                      size={10}
                      className={styles.groupCardContent}
                    >
                      <Space wrap>
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
                      <div className={styles.scopeMeta}>
                        <TeamOutlined />
                        <div>
                          <div className={styles.scopeSummary}>
                            {normalizedScopeType
                              ? `${
                                  scopeTypeText[normalizedScopeType] || '范围'
                                }：`
                              : ''}
                            {scopeText}
                          </div>
                          <Text type="secondary">
                            {group.memberCount ?? 0} 人
                          </Text>
                        </div>
                      </div>
                      <div className={styles.groupFooter}>
                        <span>
                          补卡上限 {group.monthlyCorrectionLimit ?? 0} 次/月
                        </span>
                        <span>
                          迟到阈值 {group.lateThresholdMinutes ?? 0}min
                        </span>
                      </div>
                      <div className={styles.groupActions}>
                        <Button
                          type="link"
                          icon={<FileSearchOutlined />}
                          onClick={() =>
                            history.push(
                              `/attendance/record?groupId=${group.id}`,
                            )
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
          <Card
            bordered={false}
            className={styles.ruleCard}
            extra={
              <Space wrap>
                <DatePicker
                  picker="year"
                  allowClear={false}
                  value={dayjs(`${calendarConfigYear}`, 'YYYY')}
                  onChange={(value) => {
                    if (!value) {
                      return;
                    }
                    const nextYear = value.year();
                    setCalendarConfigYear(nextYear);
                    setCalendarPanelDate(dayjs(`${nextYear}-01-01`, 'YYYY-MM-DD'));
                  }}
                />
                <Button
                  type="primary"
                  loading={calendarConfigSaving}
                  onClick={() => void handleSaveCalendarConfig()}
                >
                  保存设置
                </Button>
              </Space>
            }
          >
            <Spin spinning={calendarConfigLoading}>
              <Title level={5}>工作日设置</Title>
              <Space wrap className={styles.weekLine}>
                {weekdayOptions.map((item) => {
                  const selected = selectedWorkdays.includes(item.value);
                  return (
                    <Button
                      key={item.value}
                      type={selected ? 'primary' : 'default'}
                      className={styles.weekdayButton}
                      onClick={() => toggleWorkday(item.value)}
                    >
                      {item.label}
                    </Button>
                  );
                })}
              </Space>
              <div className={styles.holidaySection}>
                <div className={styles.holidayHeader}>
                  <Text strong>法定节假日设置</Text>
                  <Text type="secondary">{calendarConfigYear} 年</Text>
                </div>
                <AntCalendar
                  fullscreen={false}
                  validRange={[
                    dayjs(`${calendarConfigYear}-01-01`, 'YYYY-MM-DD'),
                    dayjs(`${calendarConfigYear}-12-31`, 'YYYY-MM-DD'),
                  ]}
                  value={calendarPanelDate}
                  onPanelChange={(value) => setCalendarPanelDate(value)}
                  onSelect={toggleHolidayDate}
                  fullCellRender={(value) => {
                    const dateText = value.format('YYYY-MM-DD');
                    const selected = selectedHolidayDateSet.has(dateText);
                    return (
                      <div
                        className={`${styles.calendarCell} ${
                          selected ? styles.calendarCellSelected : ''
                        }`}
                      >
                        {value.date()}
                      </div>
                    );
                  }}
                />
                {selectedHolidayDates.length > 0 ? (
                  <Space wrap className={styles.holidayTags}>
                    {selectedHolidayDates.map((item) => (
                      <Tag
                        key={item}
                        closable
                        color="orange"
                        onClose={(event) => {
                          event.preventDefault();
                          setSelectedHolidayDates((previous) =>
                            previous.filter((date) => date !== item),
                          );
                        }}
                      >
                        {formatHolidayDateLabel(item)}
                      </Tag>
                    ))}
                  </Space>
                ) : (
                  <Text type="secondary" className={styles.holidayEmpty}>
                    当前年份暂未选择法定节假日
                  </Text>
                )}
              </div>
              <div className={styles.tipBox}>
                已选法定节假日将自动排除在工作日之外。
              </div>
            </Spin>
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
              当前已启用 {activeCount} 个考勤组；补卡申请默认每月最多 2
              次，可在考勤组内单独配置。
            </Text>
          </Card>
        </Col>
      </Row>

      <Drawer
        title={editingGroup ? '编辑考勤组' : '新增考勤组'}
        open={drawerOpen}
        width={620}
        destroyOnClose
        onClose={closeDrawer}
        extra={
          <Space>
            <Button onClick={closeDrawer}>取消</Button>
            <Button type="primary" loading={submitting} onClick={handleSubmit}>
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

          <Card size="small" className={styles.scopeFormCard}>
            <Space>
              <TeamOutlined />
              <Text strong>适用范围</Text>
            </Space>
            <Form.Item
              label="范围方式"
              name="scopeType"
              rules={[{ required: true, message: '请选择范围方式' }]}
              style={{ marginTop: 12 }}
            >
              <Radio.Group
                optionType="button"
                buttonStyle="solid"
                options={[
                  { label: '部门', value: 'DEPT' },
                  { label: '职位', value: 'POST' },
                  { label: '指定员工', value: 'EMPLOYEE' },
                ]}
              />
            </Form.Item>

            <Form.Item
              noStyle
              shouldUpdate={(prev, next) => prev.scopeType !== next.scopeType}
            >
              {({ getFieldValue }) => {
                const currentScopeType = getFieldValue('scopeType') || 'DEPT';
                if (currentScopeType === 'POST') {
                  return (
                    <Form.Item
                      label="适用职位"
                      name="postId"
                      rules={[{ required: true, message: '请选择适用职位' }]}
                    >
                      <Select
                        showSearch
                        loading={postLoading}
                        placeholder="请选择职位"
                        optionFilterProp="label"
                        options={postOptions.map((post) => ({
                          label: `${post.postName}${
                            post.deptName ? `（${post.deptName}）` : ''
                          }`,
                          value: post.id,
                        }))}
                      />
                    </Form.Item>
                  );
                }
                if (currentScopeType === 'EMPLOYEE') {
                  return (
                    <>
                      <Form.Item
                        label="员工所属部门"
                        name="employeeDeptId"
                        rules={[
                          { required: true, message: '请先选择员工所属部门' },
                        ]}
                      >
                        <TreeSelect
                          allowClear
                          showSearch
                          treeData={departmentTreeData}
                          loading={departmentLoading}
                          placeholder="请选择部门"
                          treeDefaultExpandAll
                          treeNodeFilterProp="title"
                          onChange={() => {
                            form.setFieldValue('employeeIds', []);
                          }}
                        />
                      </Form.Item>
                      <Form.Item
                        label="指定员工"
                        name="employeeIds"
                        rules={[{ required: true, message: '请选择指定员工' }]}
                      >
                        <Select
                          mode="multiple"
                          showSearch
                          allowClear
                          filterOption={false}
                          disabled={!employeeDeptId}
                          loading={employeeLoading}
                          placeholder={
                            employeeDeptId
                              ? '输入姓名或工号搜索员工'
                              : '请先选择部门'
                          }
                          onFocus={() => searchEmployees('', employeeDeptId)}
                          onSearch={(keyword) =>
                            searchEmployees(keyword, employeeDeptId)
                          }
                          options={employeeOptions.map((employee) => ({
                            label: `${employee.employeeName}（${employee.employeeNo}）`,
                            value: employee.id,
                          }))}
                        />
                      </Form.Item>
                    </>
                  );
                }
                return (
                  <Form.Item
                    label="适用部门"
                    name="deptIds"
                    rules={[{ required: true, message: '请选择适用部门' }]}
                  >
                    <TreeSelect
                      multiple
                      allowClear
                      showSearch
                      treeCheckable
                      treeData={departmentTreeData}
                      loading={departmentLoading}
                      placeholder="请选择一个或多个部门"
                      treeDefaultExpandAll
                      treeNodeFilterProp="title"
                      showCheckedStrategy={TreeSelect.SHOW_CHILD}
                    />
                  </Form.Item>
                );
              }}
            </Form.Item>
          </Card>

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
              <Form.Item
                label="启用状态"
                name="enabled"
                valuePropName="checked"
              >
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
                <InputNumber
                  min={0}
                  addonAfter="分钟"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="早退阈值" name="earlyLeaveThreshold">
                <InputNumber
                  min={0}
                  addonAfter="分钟"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="补卡上限" name="maxCorrectionCount">
                <InputNumber
                  min={0}
                  addonAfter="次/月"
                  style={{ width: '100%' }}
                />
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
                  <InputNumber
                    min={0}
                    addonAfter="米"
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item label="地址说明" name="locationAddress">
              <Input placeholder="如：杭州总部 A 座" />
            </Form.Item>
          </Card>
          {editingGroup ? (
            <div className={styles.deleteArea}>
              <Button danger loading={deleting} onClick={handleDeleteGroup}>
                删除考勤配置
              </Button>
            </div>
          ) : null}
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default AttendanceGroupsPage;

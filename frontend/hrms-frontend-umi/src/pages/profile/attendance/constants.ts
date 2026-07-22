/**
 * 个人中心 - 我的考勤 常量定义
 */

// ============ 请假类型选项 ============

export const LEAVE_TYPE_OPTIONS = [
  { label: '年假', value: 'ANNUAL' },
  { label: '调休', value: 'COMPASSIONATE' },
  { label: '病假', value: 'SICK' },
  { label: '事假', value: 'PERSONAL' },
  { label: '婚假', value: 'MARRIAGE' },
  { label: '产假', value: 'MATERNITY' },
  { label: '丧假', value: 'FUNERAL' },
] as const;

// ============ 考勤状态颜色映射 ============

export const STATUS_COLOR_MAP: Record<string, string> = {
  NORMAL: '#52c41a',
  LATE: '#fa8c16',
  EARLY_LEAVE: '#faad14',
  LEAVE: '#1677ff',
  HOLIDAY: '#d9d9d9',
  ABSENT: '#cf1322',
};

export const STATUS_BG_MAP: Record<string, string> = {
  NORMAL: '#f6ffed',
  LATE: '#fff7e6',
  EARLY_LEAVE: '#fffbe6',
  MISSED: '#f5f5f5',
  LEAVE: '#e6f4ff',
  HOLIDAY: '#fafafa',
  ABSENT: '#fff1f0',
};

// ============ 图例数据 ============

export const LEGEND_ITEMS = [
  { label: '正常出勤', color: '#52c41a' },
  { label: '迟到', color: '#fa8c16' },
  { label: '早退', color: '#faad14' },
  { label: '无完整打卡记录', color: '#d9d9d9' },
  { label: '请假', color: '#1677ff' },
  { label: '休息日', color: '#d9d9d9' },
  { label: '今日', color: '#1677ff', isBorder: true },
];

// ============ 审批状态映射 ============

export const APPROVAL_STATUS_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '审批中', color: 'processing' },
  2: { text: '已通过', color: 'success' },
  3: { text: '已拒绝', color: 'error' },
  4: { text: '已撤回', color: 'warning' },
};

// ============ 补卡类型映射 ============

export const CORRECTION_TYPE_MAP: Record<string, string> = {
  CLOCK_IN: '上班卡',
  CLOCK_OUT: '下班卡',
};

// ============ 星期头 ============

export const WEEKDAY_HEADERS = ['一', '二', '三', '四', '五', '六', '日'];

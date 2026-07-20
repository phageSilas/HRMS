/**
 * 业务类型枚举与映射常量
 *
 * 统一管理审批中心及各模块的业务类型编码、颜色映射、选项列表。
 * 提取自 src/pages/approval/workspace/index.tsx 和 src/pages/approval/detail/index.tsx，
 * 消除两处重复定义。
 */

// ============ 业务类型编码枚举 ============

/** 业务类型编码常量 */
export const BusinessType = {
  ENTRY: 'ENTRY',             // 入职申请
  REGULAR: 'REGULAR',         // 转正申请
  TRANSFER: 'TRANSFER',       // 调岗申请
  LEAVE: 'LEAVE',             // 离职审批
  LEAVE_REQUEST: 'LEAVE_REQUEST', // 请假审批
  CORRECTION: 'CORRECTION',   // 补卡审批
  OVERTIME: 'OVERTIME',       // 加班审批
  SALARY: 'SALARY',           // 薪资批次审批
} as const;

/** 业务类型编码字面量类型 */
export type BusinessTypeValue = (typeof BusinessType)[keyof typeof BusinessType];

// ============ 颜色映射 ============

/** 业务类型编码 → Tag 颜色映射 */
export const BUSINESS_TYPE_COLOR_MAP: Record<string, string> = {
  [BusinessType.ENTRY]: 'green',
  [BusinessType.REGULAR]: 'blue',
  [BusinessType.TRANSFER]: 'purple',
  [BusinessType.LEAVE]: 'red',
  [BusinessType.LEAVE_REQUEST]: 'orange',
  [BusinessType.CORRECTION]: 'cyan',
  [BusinessType.OVERTIME]: 'geekblue',
  [BusinessType.SALARY]: 'magenta',
};

// ============ Select 筛选选项 ============

/** 业务类型筛选下拉选项（含"全部类型"） */
export const BUSINESS_TYPE_OPTIONS = [
  { label: '全部类型', value: '' },
  { label: '入职申请', value: BusinessType.ENTRY },
  { label: '转正申请', value: BusinessType.REGULAR },
  { label: '调岗申请', value: BusinessType.TRANSFER },
  { label: '离职审批', value: BusinessType.LEAVE },
  { label: '请假审批', value: BusinessType.LEAVE_REQUEST },
  { label: '补卡审批', value: BusinessType.CORRECTION },
  { label: '加班审批', value: BusinessType.OVERTIME },
  { label: '薪资批次审批', value: BusinessType.SALARY },
];

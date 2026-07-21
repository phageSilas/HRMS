/**
 * 审批中心 API 服务
 *
 * 提供审批全流程接口，包括待办任务、审批详情、审批操作、
 * 委托管理、历史查询等。
 *
 * @module Services.Approval
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

// ============ 查询参数 ============

/** 待办/已办任务查询参数 */
export interface PendingQuery {
  /** 业务类型筛选 */
  businessType?: string;
  /** 关键词搜索（申请人姓名 / 单号） */
  keyword?: string;
  /** 起始日期 yyyy-MM-dd */
  startDate?: string;
  /** 结束日期 yyyy-MM-dd */
  endDate?: string;
  /**
   * 筛选类型
   * - "pending": 待审批
   * - "today-approved": 今日已审批
   * - "overdue": 已逾期
   */
  filterType?: string;
  /** 页码（从 1 开始） */
  pageNum?: number;
  /** 每页条数 */
  pageSize?: number;
}

/** 我的申请查询参数 */
export interface MyApplicationQuery {
  /** 申请状态筛选 */
  status?: string;
  /** 页码（从 1 开始） */
  pageNum?: number;
  /** 每页条数 */
  pageSize?: number;
}

// ============ 审批任务（列表通用） ============

/** 审批任务列表项 */
export interface ApprovalTask {
  /** 审批单 ID */
  id: number;
  /** 任务实例 ID（审批节点粒度） */
  taskId?: number;
  /** 审批单标题 */
  title: string;
  /** 申请人姓名 */
  applicantName: string;
  /** 申请人部门名称 */
  applicantDeptName?: string;
  /** 申请人头像 URL */
  applicantAvatar?: string;
  /** 业务类型编码 */
  businessType: string;
  /** 业务类型名称 */
  businessTypeName: string;
  /** 创建时间 */
  createdAt: string;
  /** 截止时间 */
  deadline?: string;
  /** 当前节点名称 */
  nodeName: string;
  /** 审批状态编码 */
  status: string;
  /** 审批状态名称 */
  statusName?: string;
  /** 是否被委托 */
  delegateFlag?: boolean;
  /** 委托备注 */
  delegateMark?: string;
  /** 是否已逾期 */
  overdue?: boolean;
}

// ============ 审批详情 ============

/** 审批流程节点 */
export interface ApprovalNode {
  /** 节点名称 */
  nodeName: string;
  /** 节点状态：已完成 / 当前 / 待处理 */
  status: 'completed' | 'current' | 'pending';
  /** 审批人姓名 */
  operatorName: string;
}

/** 审批历史记录 */
export interface ApprovalHistory {
  /** 操作人姓名 */
  operatorName: string;
  /** 操作节点名称 */
  nodeName: string;
  /** 操作类型：通过 / 拒绝 / 转交 */
  action: 'approve' | 'reject' | 'transfer';
  /** 操作类型名称 */
  actionName: string;
  /** 审批意见 */
  comment: string;
  /** 操作时间 */
  operatedAt: string;
}

/** 审批详情响应 */
export interface ApprovalDetail {
  /** 审批单 ID */
  id: number;
  /** 审批单标题 */
  title: string;
  /** 业务类型编码 */
  businessType: string;
  /** 业务类型名称 */
  businessTypeName: string;
  /** 审批单状态编码 */
  status: string;
  /** 审批单状态名称 */
  statusName: string;
  /** 申请人姓名 */
  applicantName: string;
  /** 创建时间 */
  createdAt: string;
  /** 业务表单数据（键值对，格式因业务类型而异） */
  formData: Record<string, any>;
  /** 审批节点列表（流程时间轴用） */
  approvalNodes: ApprovalNode[];
  /** 审批历史列表 */
  approvalHistory: ApprovalHistory[];
  /** 当前登录用户是否可操作 */
  currentOperator: boolean;
  /** 当前待办任务 ID */
  currentTaskId?: number;
}

// ============ 审批操作 ============

/** 审批操作请求体 */
export interface OperateData {
  /** 操作类型：通过 / 拒绝 / 转交 */
  action: 'approve' | 'reject' | 'transfer';
  /** 审批意见 */
  comment?: string;
  /** 转交目标用户 ID（仅 transfer 时必填） */
  targetUserId?: number;
}

// ============ 委托 ============

/** 委托记录 */
export interface Delegation {
  /** 委托 ID */
  id: number;
  /** 被委托人姓名 */
  delegateeName: string;
  /** 委托开始时间 */
  startTime: string;
  /** 委托结束时间 */
  endTime: string;
  /** 委托原因 */
  reason: string;
  /** 委托状态：生效中 / 已过期 / 已取消 */
  status: 'active' | 'expired' | 'cancelled';
  /** 被委托人职位 */
  position?: string;
}

/** 创建委托请求体 */
export interface DelegationCreateData {
  /** 被委托人用户 ID */
  delegateeId: number;
  /** 委托开始时间 yyyy-MM-dd */
  startTime: string;
  /** 委托结束时间 yyyy-MM-dd */
  endTime: string;
  /** 委托原因 */
  reason?: string;
}

// ============ API 方法 ============

/**
 * 获取今日已审批数量
 *
 * @returns 今日已审批的审批单数量
 */
export async function getTodayApprovedCount() {
  return request.get<{ count: number }>('/api/v1/approval/today-approved-count');
}

/**
 * 获取已逾期数量
 *
 * @returns 当前已逾期的审批单数量
 */
export async function getOverdueCount() {
  return request.get<{ count: number }>('/api/v1/approval/overdue-count');
}

/**
 * 获取待审批列表
 *
 * @param params 查询参数（业务类型、关键词等）
 * @returns 分页的待审批任务列表
 */
export async function getPendingTasks(params?: PendingQuery) {
  // 加时间戳防止 Umi 开发服务器缓存 GET 请求导致 304
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks/pending', {
    params: { ...params, _t: Date.now() },
  });
}

/**
 * 获取任务列表（支持 filterType 筛选）
 *
 * filterType 可选值：pending / today-approved / overdue
 *
 * @param params 查询参数
 * @returns 分页的审批任务列表
 */
export async function getTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks', {
    params: { ...params, _t: Date.now() },
  });
}

/**
 * 获取待审批数量（用于角标）
 *
 * @returns 待审批总数及各业务类型明细
 */
export async function getPendingCount() {
  return request.get<{ count: number; details: Array<{ bizType: string; count: number }> }>('/api/v1/approval/pending-count');
}

/**
 * 获取已审批列表（历史）
 *
 * @param params 查询参数
 * @returns 分页的已审批任务列表
 */
export async function getHistoryTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks/history', { params });
}

/**
 * 获取我发起的申请
 *
 * @param params 查询参数（状态筛选、分页）
 * @returns 分页的申请列表
 */
export async function getMyApplications(params?: MyApplicationQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/my-applications', { params });
}

/**
 * 获取审批详情
 *
 * @param id 审批单 ID
 * @returns 审批详情（含表单数据、流程节点、审批历史）
 */
export async function getApprovalDetail(id: number) {
  return request.get<ApprovalDetail>(`/api/v1/approval/${id}`);
}

/**
 * 执行审批操作
 *
 * @param id   审批单 ID
 * @param data 操作参数（动作、意见、转交目标）
 */
export async function operateApproval(id: number, data: OperateData) {
  return request.post<void>(`/api/v1/approval/${id}/operate`, data);
}

/**
 * 撤回申请
 *
 * @param id 审批单 ID
 */
export async function withdrawApproval(id: number) {
  return request.post<void>(`/api/v1/approval/${id}/withdraw`);
}

/**
 * 新建委托
 *
 * @param data 委托参数（被委托人、时间范围、原因）
 * @returns 新建委托的 ID
 */
export async function createDelegation(data: DelegationCreateData) {
  return request.post<{ id: number }>('/api/v1/approval/delegation', data);
}

/**
 * 取消委托
 *
 * @param id 委托 ID
 */
export async function cancelDelegation(id: number) {
  return request.put<void>(`/api/v1/approval/delegation/${id}/cancel`);
}

/**
 * 获取我的委托列表
 *
 * @returns 当前生效委托 + 全部委托记录
 */
export async function getMyDelegations() {
  return request.get<{ activeDelegation: Delegation | null; records: Delegation[] }>('/api/v1/approval/delegation/my');
}

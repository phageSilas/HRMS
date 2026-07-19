/**
 * 审批中心 API 服务
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

// ============ 查询参数 ============

export interface PendingQuery {
  businessType?: string;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  filterType?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface MyApplicationQuery {
  status?: string;
  pageNum?: number;
  pageSize?: number;
}

// ============ 审批任务（列表通用） ============

export interface ApprovalTask {
  id: number;
  taskId?: number;
  title: string;
  applicantName: string;
  /** 申请人部门名称 */
  applicantDeptName?: string;
  /** 申请人头像 URL */
  applicantAvatar?: string;
  businessType: string;
  businessTypeName: string;
  createdAt: string;
  deadline?: string;
  nodeName: string;
  status: string;
  statusName?: string;
  delegateFlag?: boolean;
  delegateMark?: string;
  /** 是否已逾期 */
  overdue?: boolean;
}

// ============ 审批详情 ============

export interface ApprovalNode {
  nodeName: string;
  status: 'completed' | 'current' | 'pending';
  operatorName: string;
}

export interface ApprovalHistory {
  operatorName: string;
  nodeName: string;
  action: 'approve' | 'reject' | 'transfer';
  actionName: string;
  comment: string;
  operatedAt: string;
}

export interface ApprovalDetail {
  id: number;
  title: string;
  businessType: string;
  businessTypeName: string;
  status: string;
  statusName: string;
  applicantName: string;
  createdAt: string;
  formData: Record<string, any>;
  approvalNodes: ApprovalNode[];
  approvalHistory: ApprovalHistory[];
  currentOperator: boolean;
  currentTaskId?: number;
}

// ============ 审批操作 ============

export interface OperateData {
  action: 'approve' | 'reject' | 'transfer';
  comment?: string;
  targetUserId?: number;
}

// ============ 委托 ============

export interface Delegation {
  id: number;
  delegateeName: string;
  startTime: string;
  endTime: string;
  reason: string;
  status: 'active' | 'expired' | 'cancelled';
  position?: string;
}

export interface DelegationCreateData {
  delegateeId: number;
  startTime: string;
  endTime: string;
  reason?: string;
}

// ============ API 方法 ============

/** 今日已审批数量 */
export async function getTodayApprovedCount() {
  return request.get<{ count: number }>('/api/v1/approval/today-approved-count');
}

/** 已逾期数量 */
export async function getOverdueCount() {
  return request.get<{ count: number }>('/api/v1/approval/overdue-count');
}

/** 待审批列表 */
export async function getPendingTasks(params?: PendingQuery) {
  // 加时间戳防止 Umi 开发服务器缓存 GET 请求导致 304
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks/pending', {
    params: { ...params, _t: Date.now() },
  });
}

/** 任务列表（支持 filterType 筛选：pending/today-approved/overdue） */
export async function getTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks', {
    params: { ...params, _t: Date.now() },
  });
}

/** 待审批数量（用于角标） */
export async function getPendingCount() {
  return request.get<{ count: number; details: Array<{ bizType: string; count: number }> }>('/api/v1/approval/pending-count');
}

/** 已审批列表 */
export async function getHistoryTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks/history', { params });
}

/** 我发起的申请 */
export async function getMyApplications(params?: MyApplicationQuery) {
  return request.get<PageResult<ApprovalTask>>('/api/v1/approval/my-applications', { params });
}

/** 审批详情 */
export async function getApprovalDetail(id: number) {
  return request.get<ApprovalDetail>(`/api/v1/approval/${id}`);
}

/** 审批操作 */
export async function operateApproval(id: number, data: OperateData) {
  return request.post<void>(`/api/v1/approval/${id}/operate`, data);
}

/** 撤回申请 */
export async function withdrawApproval(id: number) {
  return request.post<void>(`/api/v1/approval/${id}/withdraw`);
}

/** 新建委托 */
export async function createDelegation(data: DelegationCreateData) {
  return request.post<{ id: number }>('/api/v1/approval/delegation', data);
}

/** 取消委托 */
export async function cancelDelegation(id: number) {
  return request.put<void>(`/api/v1/approval/delegation/${id}/cancel`);
}

/** 我的委托 */
export async function getMyDelegations() {
  return request.get<{ activeDelegation: Delegation | null; records: Delegation[] }>('/api/v1/approval/delegation/my');
}

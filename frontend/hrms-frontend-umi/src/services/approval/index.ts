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
  title: string;
  applicantName: string;
  businessType: string;
  businessTypeName: string;
  createdAt: string;
  deadline: string;
  currentNodeName: string;
  status: string;
  statusName: string;
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
}

export interface DelegationCreateData {
  delegateeId: number;
  startTime: string;
  endTime: string;
  reason?: string;
}

// ============ API 方法 ============

/** 待审批列表 */
export async function getPendingTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/approval/tasks/pending', { params });
}

/** 待审批数量（用于角标） */
export async function getPendingCount() {
  return request.get<{ count: number; details: Array<{ bizType: string; count: number }> }>('/approval/pending-count');
}

/** 已审批列表 */
export async function getHistoryTasks(params?: PendingQuery) {
  return request.get<PageResult<ApprovalTask>>('/approval/tasks/history', { params });
}

/** 我发起的申请 */
export async function getMyApplications(params?: MyApplicationQuery) {
  return request.get<PageResult<ApprovalTask>>('/approval/my-applications', { params });
}

/** 审批详情 */
export async function getApprovalDetail(id: number) {
  return request.get<ApprovalDetail>(`/approval/${id}`);
}

/** 审批操作 */
export async function operateApproval(id: number, data: OperateData) {
  return request.post<void>(`/approval/${id}/operate`, data);
}

/** 撤回申请 */
export async function withdrawApproval(id: number) {
  return request.post<void>(`/approval/${id}/withdraw`);
}

/** 新建委托 */
export async function createDelegation(data: DelegationCreateData) {
  return request.post<{ id: number }>('/approval/delegation', data);
}

/** 取消委托 */
export async function cancelDelegation(id: number) {
  return request.put<void>(`/approval/delegation/${id}/cancel`);
}

/** 我的委托 */
export async function getMyDelegations() {
  return request.get<{ activeDelegation: Delegation | null; records: Delegation[] }>('/approval/delegation/my');
}

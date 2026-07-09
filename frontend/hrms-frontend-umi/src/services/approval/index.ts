/**
 * 审批中心相关接口
 * 负责人：成员 D
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface ApprovalTask {
  id: number;
  instanceId: number;
  bizType: string;
  bizId: number;
  title: string;
  applicantId: number;
  applicantName: string;
  currentNode: string;
  status: number;
  createTime: string;
}

export interface ApprovalInstance {
  id: number;
  bizType: string;
  bizId: number;
  title: string;
  applicantId: number;
  applicantName: string;
  currentNode: string;
  status: number;
  createTime: string;
  finishTime: string | null;
  tasks: ApprovalTask[];
}

export interface ApprovalPendingCount {
  count: number;
  details: {
    bizType: string;
    count: number;
  }[];
}

export interface ApprovalQuery extends PageQuery {
  bizType?: string;
}

// ============ 待办任务接口 ============

/**
 * 获取待办任务列表
 */
export async function getPendingTaskList(params: ApprovalQuery) {
  return request.get<Result<PageResult<ApprovalTask>>>('/approval/pending', { params });
}

/**
 * 获取待审批数量（跨模块接口）
 */
export async function getPendingCount() {
  return request.get<Result<ApprovalPendingCount>>('/approval/pending-count');
}

// ============ 已办任务接口 ============

/**
 * 获取已办任务列表
 */
export async function getDoneTaskList(params: ApprovalQuery) {
  return request.get<Result<PageResult<ApprovalTask>>>('/approval/done', { params });
}

// ============ 审批处理接口 ============

/**
 * 获取审批详情
 */
export async function getApprovalDetail(instanceId: number) {
  return request.get<Result<ApprovalInstance>>(`/approval/instances/${instanceId}`);
}

/**
 * 审批处理
 */
export async function approveTask(taskId: number, data: { result: number; remark: string }) {
  return request.post<Result<void>>(`/approval/tasks/${taskId}/approve`, data);
}

/**
 * 发起审批任务（跨模块接口）
 */
export async function startApproval(data: { bizType: string; bizId: number; applicantId: number }) {
  return request.post<Result<{ taskId: number }>>('/approval/start', data);
}

/**
 * 撤回审批
 */
export async function withdrawApproval(instanceId: number) {
  return request.post<Result<void>>(`/approval/instances/${instanceId}/withdraw`);
}
/**
 * 入转调离流程相关接口
 * 负责人：成员 B
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface EntryApplication {
  id: number;
  name: string;
  gender: number;
  phone: string;
  email: string;
  departmentId: number;
  departmentName: string;
  positionId: number;
  positionName: string;
  hireDate: string;
  approvalStatus: number;
  approvalInstanceId: number | null;
  createTime: string;
}

export interface RegularApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  positionName: string;
  hireDate: string;
  regularDate: string;
  approvalStatus: number;
  approvalInstanceId: number | null;
  createTime: string;
}

export interface TransferApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  fromDeptId: number;
  fromDeptName: string;
  toDeptId: number;
  toDeptName: string;
  toPositionId: number;
  toPositionName: string;
  reason: string;
  approvalStatus: number;
  approvalInstanceId: number | null;
  createTime: string;
}

export interface LeaveApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  leaveType: number;
  leaveReason: string;
  leaveDate: string;
  approvalStatus: number;
  approvalInstanceId: number | null;
  createTime: string;
}

export interface ApplicationQuery extends PageQuery {
  keyword?: string;
  approvalStatus?: number;
}

// ============ 入职申请接口 ============

/**
 * 获取入职申请列表
 */
export async function getEntryApplicationList(params: ApplicationQuery) {
  return request.get<Result<PageResult<EntryApplication>>>('/entry-applications', { params });
}

/**
 * 创建入职申请
 */
export async function createEntryApplication(data: Partial<EntryApplication>) {
  return request.post<Result<EntryApplication>>('/entry-applications', data);
}

// ============ 转正申请接口 ============

/**
 * 获取转正申请列表
 */
export async function getRegularApplicationList(params: ApplicationQuery) {
  return request.get<Result<PageResult<RegularApplication>>>('/regular-applications', { params });
}

/**
 * 创建转正申请
 */
export async function createRegularApplication(data: Partial<RegularApplication>) {
  return request.post<Result<RegularApplication>>('/regular-applications', data);
}

// ============ 调岗申请接口 ============

/**
 * 获取调岗申请列表
 */
export async function getTransferApplicationList(params: ApplicationQuery) {
  return request.get<Result<PageResult<TransferApplication>>>('/transfer-applications', { params });
}

/**
 * 创建调岗申请
 */
export async function createTransferApplication(data: Partial<TransferApplication>) {
  return request.post<Result<TransferApplication>>('/transfer-applications', data);
}

// ============ 离职申请接口 ============

/**
 * 获取离职申请列表
 */
export async function getLeaveApplicationList(params: ApplicationQuery) {
  return request.get<Result<PageResult<LeaveApplication>>>('/leave-applications', { params });
}

/**
 * 创建离职申请
 */
export async function createLeaveApplication(data: Partial<LeaveApplication>) {
  return request.post<Result<LeaveApplication>>('/leave-applications', data);
}
/**
 * 入转调离流程相关接口。
 * 入职模块已对接后端真实 /api/v1 接口，转正、调岗、离职先保留接口契约。
 */

import request from '@/utils/request';
import type { PageResult, PageQuery } from '@/types/api';

export const ApprovalStatus = {
  DRAFT: 0,
  APPROVING: 1,
  APPROVED: 2,
  REJECTED: 3,
  ENTERED: 5,
} as const;

export type ApprovalStatusValue =
  (typeof ApprovalStatus)[keyof typeof ApprovalStatus];

export interface EntryApplication {
  id: number;
  candidateName: string;
  gender?: number;
  phone: string;
  email?: string;
  idCardNo?: string;
  deptId: number;
  deptName?: string;
  postId: number;
  postName?: string;
  hireType?: number;
  probationMonth?: number;
  probationSalaryRatio?: number;
  expectedHireDate: string;
  leaderId?: number;
  remark?: string;
  approvalStatus: ApprovalStatusValue;
  approvalStatusDesc?: string;
  approvalInstanceId?: number | null;
  createTime?: string;
}

export interface EntryApplicationQuery extends PageQuery {
  keyword?: string;
  approvalStatus?: number;
  departmentId?: number;
  dateStart?: string;
  dateEnd?: string;
}

export interface EntryApplicationFormValues {
  candidateName: string;
  gender?: number;
  phone: string;
  email?: string;
  idCardNo?: string;
  deptId: number;
  postId: number;
  hireType: number;
  probationMonth: number;
  probationSalaryRatio?: number;
  expectedHireDate: string;
  leaderId?: number;
  remark?: string;
}

export interface EntryApplicationSubmitResult {
  approvalInstanceId: number;
  approvalStatus: ApprovalStatusValue;
}

export interface EntryApplicationConfirmRequest {
  actualHireDate: string;
}

export interface EntryApplicationConfirmResult {
  employeeId: number;
  employeeNo: string;
}

export interface RegularApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  positionName: string;
  hireDate: string;
  probationEndDate: string;
  remainingDays: number;
  evaluationStatus: 'pending' | 'evaluated';
  approvalStatus: number;
  createTime: string;
}

export interface TransferApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  fromDeptName: string;
  fromPostName: string;
  toDeptName: string;
  toPostName: string;
  effectiveDate: string;
  reason: string;
  approvalStatus: number;
  createTime: string;
}

export interface LeaveApplication {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  leaveType: string;
  leaveTypeName: string;
  lastWorkDate: string;
  leaveDate: string;
  handoverEmployeeName: string;
  reason: string;
  approvalStatus: number;
  createTime: string;
}

export interface ApplicationQuery extends PageQuery {
  keyword?: string;
  approvalStatus?: number;
}

export async function getEntryApplicationList(
  params: EntryApplicationQuery,
): Promise<PageResult<EntryApplication>> {
  return request.get('/api/v1/entry-applications', { params });
}

export async function createEntryApplication(
  data: EntryApplicationFormValues,
): Promise<EntryApplication> {
  return request.post('/api/v1/entry-applications', data);
}

export async function updateEntryApplication(
  id: number,
  data: EntryApplicationFormValues,
): Promise<void> {
  return request.put(`/api/v1/entry-applications/${id}`, data);
}

export async function submitEntryApplication(
  id: number,
): Promise<EntryApplicationSubmitResult> {
  return request.post(`/api/v1/entry-applications/${id}/submit`);
}

export async function confirmEntryApplication(
  id: number,
  data: EntryApplicationConfirmRequest,
): Promise<EntryApplicationConfirmResult> {
  return request.post(`/api/v1/entry-applications/${id}/confirm`, data);
}

export async function getRegularApplicationList(params: ApplicationQuery) {
  return request.get<PageResult<RegularApplication>>(
    '/api/v1/regular-applications',
    { params },
  );
}

export async function createRegularApplication(data: Partial<RegularApplication>) {
  return request.post<RegularApplication>('/api/v1/regular-applications', data);
}

export async function getTransferApplicationList(params: ApplicationQuery) {
  return request.get<PageResult<TransferApplication>>(
    '/api/v1/transfer-applications',
    { params },
  );
}

export async function createTransferApplication(
  data: Partial<TransferApplication>,
) {
  return request.post<TransferApplication>(
    '/api/v1/transfer-applications',
    data,
  );
}

export async function getLeaveApplicationList(params: ApplicationQuery) {
  return request.get<PageResult<LeaveApplication>>(
    '/api/v1/leave-applications',
    { params },
  );
}

export async function createLeaveApplication(data: Partial<LeaveApplication>) {
  return request.post<LeaveApplication>('/api/v1/leave-applications', data);
}

/**
 * 入转调离流程相关接口。
 * 所有接口均按后端 /api/v1 契约返回业务 data，不在页面重复解包 Result。
 */

import type { PageQuery, PageResult } from '@/types/api';
import request from '@/utils/request';

export const ApprovalStatus = {
  DRAFT: 0,
  APPROVING: 1,
  APPROVED: 2,
  REJECTED: 3,
  WITHDRAWN: 4,
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
  createTime?: string | number[];
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
  id?: number;
  employeeId: number;
  employeeName: string;
  employeeNo?: string;
  deptId?: number;
  departmentName?: string;
  postId?: number;
  positionName?: string;
  hireDate?: string;
  probationEndDate?: string;
  remainingDays?: number;
  evaluationStatus?: 'pending' | 'evaluated';
  approvalStatus?: number;
  approvalStatusDesc?: string;
  createTime?: string | number[];
}

export interface RegularApplicationQuery extends PageQuery {
  tab?: 'pending' | 'evaluated';
  keyword?: string;
  departmentId?: number;
}

export interface RegularApplicationApplyRequest {
  evaluateOpinion: string;
  result: 'pass' | 'extend' | 'terminate';
  salaryAdjustment?: number;
  extendMonth?: number;
}

export interface RegularApplicationApplyResult {
  success: boolean;
  approvalId?: number;
}

export interface TransferApplication {
  id: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  fromDeptName?: string;
  fromPostName?: string;
  toDeptName?: string;
  toPostName?: string;
  effectiveDate: string;
  reason?: string;
  approvalStatus?: number;
  approvalStatusDesc?: string;
  createTime?: string | number[];
}

export interface TransferApplicationQuery extends PageQuery {
  keyword?: string;
  departmentId?: number;
  approvalStatus?: number;
}

export interface TransferApplicationCreateRequest {
  employeeId: number;
  toDeptId: number;
  toPostId: number;
  toJobLevel?: string;
  toLeaderId?: number;
  effectiveDate: string;
  salaryAdjustment?: number;
  reason?: string;
}

export interface TransferApplicationCreateResult {
  id: number;
  approvalStatus?: number;
}

export interface LeaveApplication {
  id: number;
  employeeId: number;
  employeeName?: string;
  departmentName?: string;
  leaveType: string;
  leaveTypeName?: string;
  lastWorkDate: string;
  leaveDate?: string;
  handoverEmployeeName?: string;
  reason?: string;
  approvalStatus?: number;
  approvalStatusDesc?: string;
  createTime?: string | number[];
}

export interface LeaveApplicationQuery extends PageQuery {
  keyword?: string;
  departmentId?: number;
  leaveType?: string;
  approvalStatus?: number;
}

export interface LeaveApplicationCreateRequest {
  employeeId: number;
  leaveType: 'resign' | 'terminate' | 'mutual' | 'contract_end';
  leaveReason: string;
  lastWorkDate: string;
  handoverEmployeeId: number;
  remark?: string;
}

export interface LeaveApplicationCreateResult {
  id: number;
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

export async function getRegularApplicationList(
  params: RegularApplicationQuery,
): Promise<PageResult<RegularApplication>> {
  return request.get('/api/v1/regular-applications', { params });
}

export async function applyRegularApplication(
  employeeId: number,
  data: RegularApplicationApplyRequest,
): Promise<RegularApplicationApplyResult> {
  return request.post(`/api/v1/regular-applications/${employeeId}/apply`, data);
}

export async function getTransferApplicationList(
  params: TransferApplicationQuery,
): Promise<PageResult<TransferApplication>> {
  return request.get('/api/v1/transfer-applications', { params });
}

export async function createTransferApplication(
  data: TransferApplicationCreateRequest,
): Promise<TransferApplicationCreateResult> {
  return request.post('/api/v1/transfer-applications', data);
}

export async function getLeaveApplicationList(
  params: LeaveApplicationQuery,
): Promise<PageResult<LeaveApplication>> {
  return request.get('/api/v1/leave-applications', { params });
}

export async function createLeaveApplication(
  data: LeaveApplicationCreateRequest,
): Promise<LeaveApplicationCreateResult> {
  return request.post('/api/v1/leave-applications', data);
}

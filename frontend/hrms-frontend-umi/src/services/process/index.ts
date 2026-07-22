/**
 * 入转调离流程相关接口。
 * 所有接口均按后端 /api/v1 契约返回业务 data，不在页面重复解包 Result。
 */

import type { PageQuery, PageResult } from '@/types/api';
import request from '@/utils/request';
import type { Dayjs } from 'dayjs';

export const ApprovalStatus = {
  DRAFT: 0,
  APPROVING: 1,
  APPROVED: 2,
  REJECTED: 3,
  WITHDRAWN: 4,
  ENTERED: 5,
} as const;

/** 审批状态值联合类型，供页面渲染审批状态标签时复用。 */
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
  expectedHireDate: string | number[];
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
  expectedHireDate: string | number[] | Dayjs;
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

export interface EntryApplicationStats {
  all: number;
  draft: number;
  approving: number;
  approved: number;
  rejected: number;
  entered: number;
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

/**
 * 查询入职申请分页列表。
 * 供入职管理页列表筛选与统计联动使用。
 */
export async function getEntryApplicationList(
  params: EntryApplicationQuery,
): Promise<PageResult<EntryApplication>> {
  return request.get('/api/v1/entry-applications', { params });
}

/** 查询单条入职申请详情，供编辑草稿和补全表单初始值使用。 */
export async function getEntryApplication(
  id: number,
): Promise<EntryApplication> {
  return request.get(`/api/v1/entry-applications/${id}`);
}

/** 查询入职申请状态统计，供入职管理页顶部统计卡片展示。 */
export async function getEntryApplicationStats(
  params: Omit<
    EntryApplicationQuery,
    'pageNum' | 'pageSize' | 'approvalStatus'
  >,
): Promise<EntryApplicationStats> {
  return request.get('/api/v1/entry-applications/stats', { params });
}

/** 创建入职申请草稿，供入职表单首次保存时调用。 */
export async function createEntryApplication(
  data: EntryApplicationFormValues,
): Promise<EntryApplication> {
  return request.post('/api/v1/entry-applications', data);
}

/** 更新已有入职申请草稿，供入职表单编辑保存时调用。 */
export async function updateEntryApplication(
  id: number,
  data: EntryApplicationFormValues,
): Promise<EntryApplication> {
  return request.put(`/api/v1/entry-applications/${id}`, data);
}

/** 提交入职申请进入审批流程，供入职列表“提交审批”按钮调用。 */
export async function submitEntryApplication(
  id: number,
): Promise<EntryApplicationSubmitResult> {
  return request.post(`/api/v1/entry-applications/${id}/submit`);
}

/** 快速审批通过入职申请，供入职列表“快速审批”按钮调用。 */
export async function quickApproveEntryApplication(id: number): Promise<void> {
  return request.post(`/api/v1/entry-applications/${id}/quick-approve`);
}

/** 确认员工实际入职，用于审批通过后的正式入职落库。 */
export async function confirmEntryApplication(
  id: number,
  data: EntryApplicationConfirmRequest,
): Promise<EntryApplicationConfirmResult> {
  return request.post(`/api/v1/entry-applications/${id}/confirm`, data);
}

/** 查询转正申请分页列表，供待转正/已评估标签页共用。 */
export async function getRegularApplicationList(
  params: RegularApplicationQuery,
): Promise<PageResult<RegularApplication>> {
  return request.get('/api/v1/regular-applications', { params });
}

/** 发起转正评估审批，供转正管理抽屉表单提交使用。 */
export async function applyRegularApplication(
  employeeId: number,
  data: RegularApplicationApplyRequest,
): Promise<RegularApplicationApplyResult> {
  return request.post(`/api/v1/regular-applications/${employeeId}/apply`, data);
}

/** 快速审批通过转正申请，供转正列表“快速审批”按钮调用。 */
export async function quickApproveRegularApplication(
  id: number,
): Promise<void> {
  return request.post(`/api/v1/regular-applications/${id}/quick-approve`);
}

/** 查询调岗申请分页列表，供调岗管理页筛选和展示。 */
export async function getTransferApplicationList(
  params: TransferApplicationQuery,
): Promise<PageResult<TransferApplication>> {
  return request.get('/api/v1/transfer-applications', { params });
}

/** 创建调岗申请并进入审批流程，供调岗表单提交使用。 */
export async function createTransferApplication(
  data: TransferApplicationCreateRequest,
): Promise<TransferApplicationCreateResult> {
  return request.post('/api/v1/transfer-applications', data);
}

/** 快速审批通过调岗申请，供调岗列表“快速审批”按钮调用。 */
export async function quickApproveTransferApplication(
  id: number,
): Promise<void> {
  return request.post(`/api/v1/transfer-applications/${id}/quick-approve`);
}

/** 查询离职申请分页列表，供离职管理页筛选和展示。 */
export async function getLeaveApplicationList(
  params: LeaveApplicationQuery,
): Promise<PageResult<LeaveApplication>> {
  return request.get('/api/v1/leave-applications', { params });
}

/** 创建离职申请并进入审批流程，供离职表单提交使用。 */
export async function createLeaveApplication(
  data: LeaveApplicationCreateRequest,
): Promise<LeaveApplicationCreateResult> {
  return request.post('/api/v1/leave-applications', data);
}

/** 快速审批通过离职申请，供离职列表“快速审批”按钮调用。 */
export async function quickApproveLeaveApplication(id: number): Promise<void> {
  return request.post(`/api/v1/leave-applications/${id}/quick-approve`);
}

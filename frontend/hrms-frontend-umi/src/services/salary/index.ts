/**
 * 薪资管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';

// ============ 薪资账套类型 ============

export interface SalaryTemplateItem {
  id?: number;
  itemCode: string;
  itemName: string;
  category: string;
  calcRule?: string;
  defaultValue?: number | string;
  sortNo?: number;
}

export interface SalaryTemplate {
  id: number;
  templateName: string;
  templateCode?: string;
  scopeType?: string;
  scopeValue?: string;
  scopeName?: string;
  effectiveDate?: string;
  status?: number;
  itemCount?: number;
  remark?: string;
  createTime?: string;
  items: SalaryTemplateItem[];
}

export interface SalaryTemplateQuery {
  templateName?: string;
  scope?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface SalaryTemplateCreateOrUpdateRequest {
  templateName: string;
  templateCode?: string;
  scopeType?: string;
  scopeValue?: string;
  effectiveDate?: string;
  status?: number;
  remark?: string;
  items?: SalaryTemplateItem[];
}

// ============ 薪资核算类型 ============

export interface SalaryBatch {
  id: number;
  batchNo?: string;
  salaryMonth: string;
  scopeType?: string;
  scopeValue?: string;
  batchStatus: string;
  approvalInstanceId?: number | null;
  totalCount?: number;
  totalGrossSalary?: number | string;
  totalNetSalary?: number | string;
  yellowWarningCount?: number;
  redWarningCount?: number;
  blockCount?: number;
}

export interface SalaryBatchItem {
  id: number;
  batchId: number;
  employeeId: number;
  employeeNo?: string;
  employeeName?: string;
  deptName?: string;
  baseSalary?: number | string;
  allowance?: number | string;
  performanceBonus?: number | string;
  overtimePay?: number | string;
  lateDeduction?: number | string;
  leaveDeduction?: number | string;
  socialInsurance?: number | string;
  housingFund?: number | string;
  incomeTax?: number | string;
  grossSalary?: number | string;
  deductionTotal?: number | string;
  netSalary?: number | string;
  warningLevel?: string;
  warningReason?: string;
}

export interface SalaryBatchPreview {
  batch: SalaryBatch;
  items: SalaryBatchItem[];
}

export interface SalaryBatchCreateRequest {
  salaryMonth?: string;
  month?: string;
  scopeType?: string;
  scopeValue?: string;
  employeeIds?: number[];
  templateIds?: number[];
}

export interface SalaryBatchTrendItem {
  month: string;
  grossSalary?: number | string;
  netSalary?: number | string;
  employeeCount?: number;
}

export interface SalaryBatchTrendQuery {
  anchorMonth: string;
  months?: number;
  scopeType?: string;
  scopeValue?: string;
}

export interface SalaryBatchCurrentQuery {
  salaryMonth: string;
  scopeType?: string;
  scopeValue?: string;
}

export interface SalaryBatchAdjustmentItem {
  itemCode: string;
  adjustAmount: number | string;
  reason: string;
}

export interface SalaryBatchAdjustmentRequest {
  employeeId: number;
  adjustments: SalaryBatchAdjustmentItem[];
}

// ============ 工资条类型 ============

export interface SalaryPayslipVerifyResult {
  success?: boolean;
  token?: string;
  expireTime?: string;
}

export interface SalaryManagePayslipQuery {
  keyword?: string;
  month?: string;
  deptId?: number;
  viewStatus?: 'VIEWED' | 'UNVIEWED' | 'UNPUBLISHED';
  pageNum?: number;
  pageSize?: number;
}

export interface SalaryManagePayslip {
  id: number;
  batchId: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  deptId?: number;
  deptName?: string;
  salaryMonth?: string;
  grossSalary?: number | string;
  deductionTotal?: number | string;
  netSalary?: number | string;
  batchStatus?: string;
  publishStatus?: string;
  viewStatus?: 'VIEWED' | 'UNVIEWED' | 'UNPUBLISHED' | string;
  verified?: boolean;
}

export interface SalaryManagePayslipVerifyRequest {
  password: string;
}

export interface SalaryPayslipListItem {
  id: number;
  salaryMonth: string;
  grossSalary?: number | string;
  deductionTotal?: number | string;
  netSalary?: number | string;
  batchStatus?: string;
  verified?: boolean;
}

export interface SalaryPayslipDetail extends SalaryBatchItem {
  salaryMonth?: string;
  batchNo?: string;
}

// ============ 账套接口 ============

export async function getSalaryTemplateList(params: SalaryTemplateQuery) {
  return request.get<{
    records: SalaryTemplate[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/api/v1/salary/templates', { params });
}

export async function createSalaryTemplate(data: SalaryTemplateCreateOrUpdateRequest) {
  return request.post<SalaryTemplate>('/api/v1/salary/templates', data);
}

export async function updateSalaryTemplate(
  id: number,
  data: SalaryTemplateCreateOrUpdateRequest,
) {
  return request.put<SalaryTemplate>(`/api/v1/salary/templates/${id}`, data);
}

export async function getEmployeeSalaryAccount(employeeId: number) {
  return request.get<{
    employeeId: number;
    salaryAccountId: number;
    salaryAccountName: string;
    baseSalary: number;
    probationSalaryRatio: number;
  }>(`/api/v1/salary/employees/${employeeId}/profile`);
}

// ============ 薪资核算接口 ============

export async function createSalaryBatch(data: SalaryBatchCreateRequest) {
  return request.post<SalaryBatch>('/api/v1/salary/batches', data);
}

export async function getCurrentSalaryBatch(params: SalaryBatchCurrentQuery) {
  return request.get<SalaryBatch | null>('/api/v1/salary/batches/current', { params });
}

export async function getSalaryBatchTrend(params: SalaryBatchTrendQuery) {
  return request.get<SalaryBatchTrendItem[]>('/api/v1/salary/batches/trend', { params });
}

export async function calculateSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/calculate`);
}

export async function previewSalaryBatch(batchId: number) {
  return request.get<SalaryBatchPreview>(`/api/v1/salary/batches/${batchId}/preview`);
}

export async function saveSalaryBatchAdjustments(
  batchId: number,
  data: SalaryBatchAdjustmentRequest,
) {
  return request.post<SalaryBatchItem>(`/api/v1/salary/batches/${batchId}/adjustments`, data);
}

export async function recalculateSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/recalculate`);
}

export async function submitSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/submit`);
}

// ============ 员工端工资条接口 ============

export async function getPayslipList(params: { month?: string }) {
  return request.get<SalaryPayslipListItem[]>('/api/v1/salary/payslips', { params });
}

export async function verifyPayslip(data: { month: string; password: string }) {
  return request.post<SalaryPayslipVerifyResult>('/api/v1/salary/payslip/verify', data);
}

export async function getPayslipDetail(id: number) {
  return request.get<SalaryPayslipDetail>(`/api/v1/salary/payslip/${id}`);
}

// ============ 管理端工资条接口 ============

export async function verifyManagePayslip(data: SalaryManagePayslipVerifyRequest) {
  return request.post<SalaryPayslipVerifyResult>('/api/v1/salary/manage/payslip/verify', data);
}

export async function getManagePayslipList(params: SalaryManagePayslipQuery) {
  return request.get<{
    records: SalaryManagePayslip[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/api/v1/salary/manage/payslips', { params });
}

export async function getManagePayslipDetail(id: number) {
  return request.get<SalaryPayslipDetail>(`/api/v1/salary/manage/payslip/${id}`);
}

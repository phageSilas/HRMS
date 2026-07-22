/**
 * 薪资管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

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
  employeeId?: number;
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

export interface SalaryEmployeeProfileHistoryItem {
  id: number;
  employeeId: number;
  templateIdBefore?: number;
  templateNameBefore?: string;
  templateIdAfter?: number;
  templateNameAfter?: string;
  baseSalaryBefore?: number | string;
  baseSalaryAfter?: number | string;
  allowanceBefore?: number | string;
  allowanceAfter?: number | string;
  performanceBaseBefore?: number | string;
  performanceBaseAfter?: number | string;
  socialInsuranceBaseBefore?: number | string;
  socialInsuranceBaseAfter?: number | string;
  housingFundBaseBefore?: number | string;
  housingFundBaseAfter?: number | string;
  probationSalaryRatioBefore?: number | string;
  probationSalaryRatioAfter?: number | string;
  changeReason?: string;
  createTime?: string;
  createBy?: number;
}

export interface SalaryEmployeeProfileDetail {
  employeeId: number;
  employeeNo?: string;
  employeeName?: string;
  deptId?: number;
  deptName?: string;
  postId?: number;
  postName?: string;
  employmentStatus?: number;
  employmentStatusDesc?: string;
  templateId?: number;
  templateName?: string;
  assignedTemplate?: boolean;
  baseSalary?: number | string;
  allowance?: number | string;
  performanceBase?: number | string;
  socialInsuranceBase?: number | string;
  housingFundBase?: number | string;
  probationSalaryRatio?: number | string;
  effectiveDate?: string;
  remark?: string;
  history?: SalaryEmployeeProfileHistoryItem[];
}

export interface SalaryEmployeeProfileUpdateRequest {
  templateId?: number;
  baseSalary: number | string;
  allowance?: number | string;
  performanceBase?: number | string;
  socialInsuranceBase?: number | string;
  housingFundBase?: number | string;
  probationSalaryRatio?: number | string;
  effectiveDate?: string;
  remark?: string;
  changeReason?: string;
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
  deptId?: number;
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
  pensionInsurance?: number | string;
  medicalInsurance?: number | string;
  unemploymentInsurance?: number | string;
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
  deptIds?: number[];
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

export interface SalaryBatchExportResult {
  fileId: number;
  fileName?: string;
  downloadUrl: string;
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

export interface SalaryPayslipPageQuery {
  month?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface SalaryTrendItem {
  month: string;
  netSalary?: number | string;
}

// ============ 账套接口 ============

/** 查询薪资账套列表，供账套管理页分页筛选展示。 */
export async function getSalaryTemplateList(params: SalaryTemplateQuery) {
  return request.get<{
    records: SalaryTemplate[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/api/v1/salary/templates', { params });
}

/** 创建薪资账套，供账套抽屉表单首次保存时调用。 */
export async function createSalaryTemplate(data: SalaryTemplateCreateOrUpdateRequest) {
  return request.post<SalaryTemplate>('/api/v1/salary/templates', data);
}

/** 更新薪资账套，供账套编辑场景保存变更时调用。 */
export async function updateSalaryTemplate(
  id: number,
  data: SalaryTemplateCreateOrUpdateRequest,
) {
  return request.put<SalaryTemplate>(`/api/v1/salary/templates/${id}`, data);
}

/** 查询员工薪资账户基础信息，供外部页面快速获取单个员工当前账套配置。 */
export async function getEmployeeSalaryAccount(employeeId: number) {
  return request.get<{
    employeeId: number;
    salaryAccountId: number;
    salaryAccountName: string;
    baseSalary: number;
    probationSalaryRatio: number;
  }>(`/api/v1/salary/employees/${employeeId}/profile`);
}

/** 查询员工薪资档案详情，供薪资档案页面加载明细与变更历史。 */
export async function getSalaryEmployeeProfileDetail(employeeId: number) {
  return request.get<SalaryEmployeeProfileDetail>('/api/v1/salary/employees/detail', {
    params: { employeeId },
  });
}

/** 更新员工薪资档案，供薪资档案编辑弹窗提交保存。 */
export async function updateSalaryEmployeeProfile(
  employeeId: number,
  data: SalaryEmployeeProfileUpdateRequest,
) {
  return request.put<SalaryEmployeeProfileDetail>(`/api/v1/salary/employees/${employeeId}/profile`, data);
}

// ============ 薪资核算接口 ============

/** 创建薪资批次草稿，供薪资批次页面按月份启动核算流程。 */
export async function createSalaryBatch(data: SalaryBatchCreateRequest) {
  return request.post<SalaryBatch>('/api/v1/salary/batches', data);
}

/** 查询当前月份薪资批次，供批次工作台初始化和刷新时使用。 */
export async function getCurrentSalaryBatch(params: SalaryBatchCurrentQuery) {
  return request.get<SalaryBatch | null>('/api/v1/salary/batches/current', { params });
}

/** 查询薪资批次趋势数据，供批次页趋势图展示。 */
export async function getSalaryBatchTrend(params: SalaryBatchTrendQuery) {
  return request.get<SalaryBatchTrendItem[]>('/api/v1/salary/batches/trend', { params });
}

/** 触发薪资批次计算，供新批次或草稿批次进入核算阶段。 */
export async function calculateSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/calculate`);
}

/** 预览薪资批次明细，供批次页展示员工级核算结果。 */
export async function previewSalaryBatch(batchId: number) {
  return request.get<SalaryBatchPreview>(`/api/v1/salary/batches/${batchId}/preview`);
}

/** 保存批次明细调整项，供人工复核后回写调整数据。 */
export async function saveSalaryBatchAdjustments(
  batchId: number,
  data: SalaryBatchAdjustmentRequest,
) {
  return request.post<SalaryBatchItem>(`/api/v1/salary/batches/${batchId}/adjustments`, data);
}

/** 重新计算薪资批次，供人工调整后重新生成结果。 */
export async function recalculateSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/recalculate`);
}

/** 提交薪资批次进入审批流程。 */
export async function submitSalaryBatch(batchId: number) {
  return request.post<SalaryBatch>(`/api/v1/salary/batches/${batchId}/submit`);
}

/** 导出薪资批次文件，供管理端下载核算结果。 */
export async function exportSalaryBatch(batchId: number) {
  return request.post<SalaryBatchExportResult>(`/api/v1/salary/batches/${batchId}/export`);
}

// ============ 员工端工资条接口 ============

/** 查询员工工资条列表，供员工端按月份浏览工资条。 */
export async function getPayslipList(params: { month?: string }) {
  return request.get<SalaryPayslipListItem[]>('/api/v1/salary/payslips', { params });
}

/** 分页查询工资条列表，供员工端分页页签或列表组件复用。 */
export async function getPayslipPage(params: SalaryPayslipPageQuery) {
  return request.get<PageResult<SalaryPayslipListItem>>('/api/v1/salary/payslips/page', {
    params,
  });
}

/** 查询个人薪资趋势，供员工端趋势图展示。 */
export async function getSalaryTrend() {
  return request.get<SalaryTrendItem[]>('/api/v1/salary/trend');
}

/** 校验工资条查看密码，供员工端查看敏感工资条前二次验证。 */
export async function verifyPayslip(data: { month: string; password: string }) {
  return request.post<SalaryPayslipVerifyResult>('/api/v1/salary/payslip/verify', data);
}

/** 查询员工端工资条详情。 */
export async function getPayslipDetail(id: number) {
  return request.get<SalaryPayslipDetail>(`/api/v1/salary/payslip/${id}`);
}

// ============ 管理端工资条接口 ============

/** 校验管理端工资条查看密码。 */
export async function verifyManagePayslip(data: SalaryManagePayslipVerifyRequest) {
  return request.post<SalaryPayslipVerifyResult>('/api/v1/salary/manage/payslip/verify', data);
}

/** 查询管理端工资条列表，供工资条管理页面筛选和分页展示。 */
export async function getManagePayslipList(params: SalaryManagePayslipQuery) {
  return request.get<{
    records: SalaryManagePayslip[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/api/v1/salary/manage/payslips', { params });
}

/** 查询管理端工资条详情，供工资条详情弹窗展示。 */
export async function getManagePayslipDetail(id: number) {
  return request.get<SalaryPayslipDetail>(`/api/v1/salary/manage/payslip/${id}`);
}

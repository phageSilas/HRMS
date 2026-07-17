/**
 * 薪资管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';
import type { PageQuery, PageResult } from '@/types/api';

// ============ 类型定义 ============

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

export interface SalaryTemplateQuery extends Partial<PageQuery> {
  templateName?: string;
  scope?: string;
  status?: number;
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

export interface SalaryBatch {
  id: number;
  yearMonth: string;
  status: number;
  totalCount: number;
  totalAmount: number;
  createTime: string;
}

export interface SalaryDetail {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  yearMonth: string;
  baseSalary: number;
  bonus: number;
  deduction: number;
  actualSalary: number;
  status: number;
}

export interface Payslip {
  employeeId: number;
  employeeName: string;
  yearMonth: string;
  items: {
    name: string;
    type: number;
    amount: number;
  }[];
  totalIncome: number;
  totalDeduction: number;
  actualSalary: number;
}

// ============ 薪资账套接口 ============

/**
 * 获取薪资账套列表
 */
export async function getSalaryTemplateList(params: SalaryTemplateQuery) {
  return request.get<PageResult<SalaryTemplate>>('/api/v1/salary/templates', { params });
}

/**
 * 创建薪资账套
 */
export async function createSalaryTemplate(data: SalaryTemplateCreateOrUpdateRequest) {
  return request.post<SalaryTemplate>('/api/v1/salary/templates', data);
}

/**
 * 更新薪资账套
 */
export async function updateSalaryTemplate(
  id: number,
  data: SalaryTemplateCreateOrUpdateRequest,
) {
  return request.put<SalaryTemplate>(`/api/v1/salary/templates/${id}`, data);
}

/**
 * 获取员工薪资档案（跨模块接口）
 */
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

/**
 * 获取薪资批次列表
 */
export async function getSalaryBatchList(params: PageQuery & { yearMonth?: string }) {
  return request.get<PageResult<SalaryBatch>>('/salary/batches', { params });
}

/**
 * 发起薪资核算
 */
export async function calculateSalary(data: { yearMonth: string; departmentIds?: number[] }) {
  return request.post<SalaryBatch>('/salary/calculate', data);
}

// ============ 工资条接口 ============

/**
 * 获取工资条列表
 */
export async function getPayslipList(params: PageQuery & { yearMonth?: string }) {
  return request.get<PageResult<SalaryDetail>>('/salary/payslips', { params });
}

/**
 * 获取工资条详情
 */
export async function getPayslipDetail(employeeId: number, yearMonth: string) {
  return request.get<Payslip>(`/salary/payslips/${employeeId}/${yearMonth}`);
}

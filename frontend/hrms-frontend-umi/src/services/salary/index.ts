/**
 * 薪资管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface SalaryAccount {
  id: number;
  name: string;
  description: string;
  baseSalary: number;
  probationSalaryRatio: number;
  status: number;
}

export interface SalaryItem {
  id: number;
  accountId: number;
  name: string;
  type: number;  // 1-应发 2-应扣
  formula: string;
  sort: number;
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

export interface SalaryAccountQuery extends PageQuery {
  keyword?: string;
  status?: number;
}

// ============ 薪资账套接口 ============

/**
 * 获取薪资账套列表
 */
export async function getSalaryAccountList(params: SalaryAccountQuery) {
  return request.get<Result<PageResult<SalaryAccount>>>('/salary-accounts', { params });
}

/**
 * 获取薪资账套详情
 */
export async function getSalaryAccountDetail(id: number) {
  return request.get<Result<SalaryAccount>>(`/salary-accounts/${id}`);
}

/**
 * 获取员工薪资档案（跨模块接口）
 */
export async function getEmployeeSalaryAccount(employeeId: number) {
  return request.get<Result<{ employeeId: number; salaryAccountId: number; salaryAccountName: string; baseSalary: number; probationSalaryRatio: number }>>(`/salary/account/${employeeId}`);
}

// ============ 薪资核算接口 ============

/**
 * 获取薪资批次列表
 */
export async function getSalaryBatchList(params: PageQuery & { yearMonth?: string }) {
  return request.get<Result<PageResult<SalaryBatch>>>('/salary/batches', { params });
}

/**
 * 发起薪资核算
 */
export async function calculateSalary(data: { yearMonth: string; departmentIds?: number[] }) {
  return request.post<Result<SalaryBatch>>('/salary/calculate', data);
}

// ============ 工资条接口 ============

/**
 * 获取工资条列表
 */
export async function getPayslipList(params: PageQuery & { yearMonth?: string }) {
  return request.get<Result<PageResult<SalaryDetail>>>('/salary/payslips', { params });
}

/**
 * 获取工资条详情
 */
export async function getPayslipDetail(employeeId: number, yearMonth: string) {
  return request.get<Result<Payslip>>(`/salary/payslips/${employeeId}/${yearMonth}`);
}
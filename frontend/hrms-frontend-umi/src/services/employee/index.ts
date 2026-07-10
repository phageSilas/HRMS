/**
 * 员工档案相关接口
 * 负责人：成员 B
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface Employee {
  id: number;
  employeeNo: string;
  name: string;
  gender: number;
  phone: string;
  email: string;
  idCard: string;
  departmentId: number;
  departmentName: string;
  positionId: number;
  positionName: string;
  jobLevel: string;
  leaderId: number | null;
  leaderName: string | null;
  hireDate: string;
  employmentStatus: number;
  baseSalary: number;
}

export interface EmployeeBrief {
  id: number;
  name: string;
  employeeNo: string;
  departmentId: number;
  departmentName: string;
  employmentStatus: number;
}

export interface EmployeeQuery extends PageQuery {
  keyword?: string;
  departmentId?: number;
  employmentStatus?: number;
}

export interface EmployeeCreateRequest {
  name: string;
  gender: number;
  phone: string;
  email?: string;
  idCard: string;
  departmentId: number;
  positionId: number;
  jobLevel?: string;
  leaderId?: number;
  hireDate: string;
  baseSalary?: number;
}

// ============ 接口定义 ============

/**
 * 获取员工列表
 */
export async function getEmployeeList(params: EmployeeQuery) {
  return request.get<Result<PageResult<Employee>>>('/employees', { params });
}

/**
 * 获取员工详情
 */
export async function getEmployeeDetail(id: number) {
  return request.get<Result<Employee>>(`/employees/${id}`);
}

/**
 * 获取员工简要信息（跨模块接口）
 */
export async function getEmployeeBrief(id: number) {
  return request.get<Result<EmployeeBrief>>(`/employees/brief/${id}`);
}

/**
 * 获取员工完整档案（跨模块接口）
 */
export async function getEmployeeFull(id: number) {
  return request.get<Result<Employee>>(`/employees/full/${id}`);
}

/**
 * 创建员工
 */
export async function createEmployee(data: EmployeeCreateRequest) {
  return request.post<Result<Employee>>('/employees', data);
}

/**
 * 更新员工
 */
export async function updateEmployee(id: number, data: Partial<EmployeeCreateRequest>) {
  return request.put<Result<Employee>>(`/employees/${id}`, data);
}

/**
 * 删除员工
 */
export async function deleteEmployee(id: number) {
  return request.delete<Result<void>>(`/employees/${id}`);
}

/**
 * 生成工号（跨模块接口）
 */
export async function generateEmployeeNo(departmentId: number) {
  return request.post<Result<{ employeeNo: string }>>('/employees/gen-no', { departmentId });
}

/**
 * 按部门获取员工列表（跨模块接口）
 */
export async function getEmployeesByDepartment(departmentId: number) {
  return request.get<Result<EmployeeBrief[]>>(`/employees/by-department/${departmentId}`);
}
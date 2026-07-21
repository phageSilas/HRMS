/**
 * 员工档案相关接口
 * 负责人：成员 B
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

// ============ 类型定义 ============

/** 员工简要信息（列表用，字段与后端 EmployeeListVO 对齐） */
export interface EmployeeBrief {
  id: number;
  employeeName: string;
  employeeNo: string;
  deptName: string;
  postName: string;
  jobLevel: string;
  employmentStatus: number;
  hireDate: string;
}

/** 员工完整信息（详情/编辑用，字段与后端 VO 对齐） */
export interface Employee {
  id: number;
  employeeName: string;
  employeeNo: string;
  gender: number;
  birthday: string;
  phone: string;
  email: string;
  idCardNo: string;
  emergencyContact: string;
  emergencyPhone: string;
  deptId: number;
  deptName: string;
  postId: number;
  postName: string;
  jobLevel: string;
  hireDate: string;
  probationMonth: number;
  probationEndDate: string;
  employmentStatus: number;
  contractType: number;
  contractExpireDate: string;
  bankAccount: string;
  bankName: string;
  workLocation: string;
  hireType: number;
  leaderId: number;
  domicileAddress: string;
  currentAddress: string;
  baseSalary: number;
  salaryTemplateId: number;
}

/** 员工查询参数（字段与后端 EmployeeQueryDTO 对齐） */
export interface EmployeeQuery {
  keyword?: string;
  deptIds?: number[];
  employmentStatus?: number[];
  jobLevel?: string;
  hireDateStart?: string;
  hireDateEnd?: string;
  pageNum?: number;
  pageSize?: number;
}

/** 创建/更新员工请求（字段与后端 EmployeeCreateDTO 对齐） */
export interface EmployeeCreateRequest {
  employeeName: string;
  gender?: number;            // 1-男 2-女
  birthday?: string;          // 后端字段名
  phone: string;              // 后端必填
  email?: string;
  idCardNo?: string;          // 后端字段名
  emergencyContact?: string;
  emergencyPhone?: string;
  deptId: number;             // 后端字段名（不是 departmentId）
  postId?: number;            // 后端字段名（不是 positionId）
  jobLevel?: string;          // 后端字段名（不是 grade）
  hireDate: string;
  probationMonth?: number;    // 后端字段名（不是 probationMonths）
  contractType?: number;      // 后端 Integer 类型
  contractExpireDate?: string;// 后端字段名
  leaderId?: number;
  workLocation?: string;
  hireType?: number;
  probationSalaryRatio?: number;
  salaryTemplateId?: number;
  baseSalary?: number;
  domicileAddress?: string;
  currentAddress?: string;
  bankAccount?: string;
  bankName?: string;
  remark?: string;
}

/** 字段权限 */
export interface FieldPermissions {
  /** 可查看字段列表，["*"] 表示全部 */
  viewableFields: string[];
  /** 可编辑字段列表，["*"] 表示全部 */
  editableFields: string[];
  /** 流程必填字段列表（需走审批流程，不可直接编辑） */
  flowRequiredFields: string[];
}

/** 合同信息（字段与后端 EmployeeContractVO 对齐） */
export interface Contract {
  id: number;
  employeeId: number;
  contractNo?: string;
  /** 合同类型：1-固定期限 2-无固定期限 3-劳务合同 */
  contractType: number;
  contractTypeDesc?: string;
  /** 合同开始日期 */
  startDate: string;
  /** 合同结束日期 */
  endDate: string;
  /** 试用期（月） */
  probationMonth?: number;
  /** 试用期薪资比例（%） */
  probationSalaryRatio?: number;
  /** 附件文件ID */
  attachmentFileId?: number;
  /** 续签次数 */
  signingCount: number;
  /** 备注 */
  remark?: string;
  /** 创建时间 */
  createTime?: string;

  // 前端扩展字段（列表展示时关联查询）
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
}

/** 创建合同请求（字段与后端 ContractCreateDTO 对齐） */
export interface ContractCreateRequest {
  employeeId: number;
  contractNo?: string;
  contractType: number;
  startDate?: string;
  endDate?: string;
  probationMonth?: number;
  probationSalaryRatio?: number;
  attachmentFileId?: number;
  remark?: string;
}

/** 更新合同请求（字段与后端 ContractUpdateDTO 对齐） */
export interface ContractUpdateRequest {
  contractNo?: string;
  contractType?: number;
  startDate?: string;
  endDate?: string;
  probationMonth?: number;
  probationSalaryRatio?: number;
  attachmentFileId?: number;
  remark?: string;
}

// ============ 接口定义 ============

/**
 * 获取员工分页列表
 */
export async function getEmployeeList(params: EmployeeQuery) {
  return request.get<PageResult<EmployeeBrief>>('/api/v1/employees', {
    params,
  });
}

/**
 * 获取员工详情
 */
export async function getEmployeeDetail(id: number) {
  return request.get<Employee>(`/api/v1/employees/${id}`);
}

/**
 * 获取员工简要信息（跨模块）
 */
export async function getEmployeeBrief(id: number) {
  return request.get<{
    employeeName: string;
    employeeNo: string;
    departmentName: string;
    positionName: string;
  }>(`/api/v1/employees/brief/${id}`);
}

/**
 * 新增员工
 */
export async function createEmployee(data: EmployeeCreateRequest) {
  return request.post<{ id: number }>('/api/v1/employees', data);
}

/**
 * 更新员工
 */
export async function updateEmployee(
  id: number,
  data: Partial<EmployeeCreateRequest>,
) {
  return request.put<{ success: boolean }>(
    `/api/v1/employees/${id}`,
    data,
  );
}

/**
 * 删除员工
 */
export async function deleteEmployee(id: number) {
  return request.delete<void>(`/api/v1/employees/${id}`);
}

/**
 * 生成工号
 * @param deptCode 部门编码（不是部门ID）
 */
export async function generateEmployeeNo(deptCode: string) {
  return request.get<{ employeeNo: string }>(
    '/api/v1/employees/gen-no',
    { params: { deptCode } },
  );
}

/**
 * 按部门获取员工列表
 */
export async function getEmployeesByDepartment(departmentId: number) {
  return request.get<EmployeeBrief[]>(
    `/api/v1/employees/by-department/${departmentId}`,
  );
}

/**
 * 获取字段权限
 */
export async function getFieldPermissions() {
  return request.get<FieldPermissions>('/api/v1/permissions/field', {
    params: { bizType: 'employee' },
  });
}

/**
 * 获取某个员工的合同列表
 */
export async function getContractsByEmployee(employeeId: number) {
  return request.get<Contract[]>(
    `/api/v1/employee-contracts/employee/${employeeId}`,
  );
}

/**
 * 获取合同详情
 */
export async function getContractDetail(id: number) {
  return request.get<Contract>(`/api/v1/employee-contracts/${id}`);
}

/**
 * 创建合同
 */
export async function createContract(data: ContractCreateRequest) {
  return request.post<Contract>('/api/v1/employee-contracts', data);
}

/**
 * 更新合同
 */
export async function updateContract(id: number, data: ContractUpdateRequest) {
  return request.put<Contract>(`/api/v1/employee-contracts/${id}`, data);
}

/**
 * 删除合同
 */
export async function deleteContract(id: number) {
  return request.delete<void>(`/api/v1/employee-contracts/${id}`);
}

/**
 * 查询全部合同列表（分页）
 * 供合同管理页使用，支持按员工姓名、工号、合同编号搜索
 */
export async function getContractList(params: {
  keyword?: string;
  contractType?: number;
  pageNum?: number;
  pageSize?: number;
}) {
  return request.get<PageResult<Contract>>('/api/v1/employee-contracts/all', {
    params,
  });
}

/**
 * 检查部门下是否有在职员工
 * @param deptId 部门ID
 * @returns true-有在职员工，false-无在职员工
 */
export async function hasEmployeesInDept(deptId: number) {
  return request.get<boolean>('/api/v1/employees/check-dept', {
    params: { deptId },
  });
}

/**
 * 检查职位下是否有在职员工
 * @param postId 职位ID
 * @returns true-有在职员工，false-无在职员工
 */
export async function hasEmployeesInPost(postId: number) {
  return request.get<boolean>('/api/v1/employees/check-post', {
    params: { postId },
  });
}

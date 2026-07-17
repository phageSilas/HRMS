/**
 * 组织架构相关接口
 * 负责人：成员 A
 */

import request from '@/utils/request';
import type { PageQuery, PageResult } from '@/types/api';

// ============ 类型定义 ============

export interface Department {
  id: number;
  deptName: string;
  parentId: number | null;
  sortNo: number;
  leaderUserId: number | null;
  leaderEmployeeId: number | null;
  children?: Department[];
}

export interface DepartmentTree {
  id: number;
  deptName: string;
  deptCode: string;
  parentId: number | null;
  children?: DepartmentTree[];
}

export interface Post {
  id: number;
  postName: string;
  postCode: string;
  sequenceCode: string;
  deptId: number | null;
  defaultProbationMonth: number;
  status: number;
  sortNo: number;
}

export interface DictData {
  id?: number;
  dictType: string;
  dictLabel?: string;
  dictValue?: string;
  label?: string;
  value?: string;
  sort: number;
  status: number;
}

// ============ 部门接口 ============

/**
 * 获取部门树
 */
export async function getDepartmentTree() {
  return request.get<DepartmentTree[]>('/api/v1/depts/tree');
}

/**
 * 获取部门列表
 */
export async function getDepartmentList() {
  return request.get<Department[]>('/api/v1/depts');
}

/**
 * 获取部门详情
 */
export async function getDepartmentDetail(id: number) {
  return request.get<Department>(`/api/v1/depts/${id}`);
}

// ============ 职位接口 ============

/**
 * 获取职位列表
 */
export async function getPostList(params?: PageQuery) {
  return request.get<PageResult<Post>>('/api/v1/posts', { params });
}

// ============ 字典接口 ============

/**
 * 按类型获取字典数据
 */
export async function getDictDataByType(dictType: string) {
  return request.get<DictData[]>(`/api/v1/dicts/data/${dictType}`);
}

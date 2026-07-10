/**
 * 组织架构相关接口
 * 负责人：成员 A
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface Department {
  id: number;
  name: string;
  parentId: number | null;
  sort: number;
  leaderId: number | null;
  leaderName: string | null;
  children?: Department[];
}

export interface DepartmentTree {
  id: number;
  name: string;
  parentId: number | null;
  children?: DepartmentTree[];
}

export interface Post {
  id: number;
  name: string;
  code: string;
  sort: number;
  status: number;
}

export interface DictData {
  id: number;
  dictType: string;
  label: string;
  value: string;
  sort: number;
  status: number;
}

// ============ 部门接口 ============

/**
 * 获取部门树
 */
export async function getDepartmentTree() {
  return request.get<Result<DepartmentTree[]>>('/departments/tree');
}

/**
 * 获取部门列表
 */
export async function getDepartmentList(params?: PageQuery) {
  return request.get<Result<PageResult<Department>>>('/departments', { params });
}

/**
 * 获取部门详情
 */
export async function getDepartmentDetail(id: number) {
  return request.get<Result<Department>>(`/departments/${id}`);
}

// ============ 职位接口 ============

/**
 * 获取职位列表
 */
export async function getPostList(params?: PageQuery) {
  return request.get<Result<PageResult<Post>>>('/posts', { params });
}

// ============ 字典接口 ============

/**
 * 按类型获取字典数据
 */
export async function getDictDataByType(dictType: string) {
  return request.get<Result<DictData[]>>(`/dict-data/type/${dictType}`);
}
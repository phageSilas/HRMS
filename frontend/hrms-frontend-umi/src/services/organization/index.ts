/**
 * 组织架构相关接口
 * 包含：部门管理、职位管理、字典管理
 */

import request from '@/utils/request';
import type { PageQuery, PageResult } from '@/types/api';

// ============ 类型定义 ============

/**
 * 部门树节点
 */
export interface DeptTreeNode {
  id: number;
  deptName: string;
  deptCode: string;
  parentId: number;
  deptLevel: number;
  leaderUserId: number | null;
  leaderEmployeeId: number | null;
  employeeCount: number;
  sortNo: number;
  status: number;
  children?: DeptTreeNode[];
}

/**
 * 部门平铺列表项
 */
export interface DeptListItem {
  id: number;
  deptName: string;
  parentId: number;
  deptLevel: number;
}

/**
 * 部门详情
 */
export interface DeptDetail {
  id: number;
  deptName: string;
  deptCode: string;
  parentId: number;
  parentName: string;
  ancestors: string;
  deptLevel: number;
  leaderUserId: number | null;
  leaderEmployeeId: number | null;
  leaderName: string;
  employeeCount: number;
  sortNo: number;
  status: number;
  remark: string;
  createTime: string;
}

/**
 * 创建部门请求
 */
export interface DeptCreateRequest {
  deptName: string;
  deptCode: string;
  parentId?: number;
  leaderUserId?: number;
  sortNo?: number;
  remark?: string;
}

/**
 * 更新部门请求
 */
export interface DeptUpdateRequest {
  deptName: string;
  leaderUserId?: number;
  sortNo?: number;
  remark?: string;
}

/**
 * 职位信息
 */
export interface PostItem {
  id: number;
  postName: string;
  postCode: string;
  sequenceCode: string;
  sequenceName: string;
  deptId: number | null;
  deptName: string;
  jobLevelMin: string;
  jobLevelMax: string;
  defaultProbationMonth: number;
  status: number;
  sortNo: number;
  createTime: string;
}

/**
 * 职位查询参数
 */
export interface PostQueryParams extends PageQuery {
  deptId?: number;
  sequenceCode?: string;
  keyword?: string;
}

/**
 * 创建职位请求
 */
export interface PostCreateRequest {
  postName: string;
  postCode: string;
  sequenceCode: string;
  deptId?: number;
  jobLevelMin?: string;
  jobLevelMax?: string;
  defaultProbationMonth?: number;
  description?: string;
  sortNo?: number;
}

/**
 * 更新职位请求
 */
export interface PostUpdateRequest {
  postName: string;
  sequenceCode: string;
  deptId?: number;
  jobLevelMin?: string;
  jobLevelMax?: string;
  defaultProbationMonth?: number;
  description?: string;
  sortNo?: number;
}

/**
 * 字典类型
 */
export interface DictTypeItem {
  id: number;
  dictName: string;
  dictType: string;
  status: number;
  remark: string;
  createTime: string;
}

/**
 * 字典数据
 */
export interface DictDataItem {
  id: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  sort: number;
  status: number;
  remark: string;
}

/**
 * 字典数据（别名，保持兼容性）
 */
export type DictData = DictDataItem;

/**
 * 创建字典类型请求
 */
export interface DictTypeCreateRequest {
  dictName: string;
  dictType: string;
  remark?: string;
}

/**
 * 创建字典数据请求
 */
export interface DictDataCreateRequest {
  dictType: string;
  dictLabel: string;
  dictValue: string;
  cssClass?: string;
  sort?: number;
  remark?: string;
}

// ============ 部门接口 ============

/**
 * 获取部门树
 */
export async function getDeptTree(): Promise<DeptTreeNode[]> {
  return request.get<DeptTreeNode[]>('/api/v1/depts/tree');
}

/**
 * 获取部门树（别名，保持兼容性）
 */
export const getDepartmentTree = getDeptTree;

/**
 * 获取部门列表
 */
export async function getDeptList(): Promise<DeptListItem[]> {
  return request.get<DeptListItem[]>('/api/v1/depts');
}

/**
 * 获取部门列表（别名，保持兼容性）
 */
export const getDepartmentList = getDeptList;

/**
 * 获取部门详情
 */
export async function getDeptDetail(id: number): Promise<DeptDetail> {
  return request.get<DeptDetail>(`/api/v1/depts/${id}`);
}

/**
 * 创建部门
 */
export async function createDept(data: DeptCreateRequest): Promise<number> {
  return request.post<number>('/api/v1/depts', data);
}

/**
 * 更新部门
 */
export async function updateDept(id: number, data: DeptUpdateRequest): Promise<void> {
  return request.put<void>(`/api/v1/depts/${id}`, data);
}

/**
 * 删除部门
 */
export async function deleteDept(id: number): Promise<void> {
  return request.delete<void>(`/api/v1/depts/${id}`);
}

// ============ 职位接口 ============

/**
 * 获取职位列表
 */
export async function getPostList(params: PostQueryParams): Promise<PageResult<PostItem>> {
  return request.get<PageResult<PostItem>>('/api/v1/posts', { params });
}

/**
 * 获取职位详情
 */
export async function getPostDetail(id: number): Promise<PostItem> {
  return request.get<PostItem>(`/api/v1/posts/${id}`);
}

/**
 * 创建职位
 */
export async function createPost(data: PostCreateRequest): Promise<number> {
  return request.post<number>('/api/v1/posts', data);
}

/**
 * 更新职位
 */
export async function updatePost(id: number, data: PostUpdateRequest): Promise<void> {
  return request.put<void>(`/api/v1/posts/${id}`, data);
}

/**
 * 删除职位
 */
export async function deletePost(id: number): Promise<void> {
  return request.delete<void>(`/api/v1/posts/${id}`);
}

/**
 * 统计各序列职位数量
 */
export async function getPostStatsBySequence(): Promise<Record<string, number>> {
  return request.get<Record<string, number>>('/api/v1/posts/stats/sequence');
}

// ============ 字典接口 ============

/**
 * 获取字典类型列表
 */
export async function getDictTypeList(params: PageQuery): Promise<PageResult<DictTypeItem>> {
  return request.get<PageResult<DictTypeItem>>('/api/v1/dicts/types', { params });
}

/**
 * 创建字典类型
 */
export async function createDictType(data: DictTypeCreateRequest): Promise<number> {
  return request.post<number>('/api/v1/dicts/types', data);
}

/**
 * 更新字典类型
 */
export async function updateDictType(id: number, data: DictTypeCreateRequest): Promise<void> {
  return request.put<void>(`/api/v1/dicts/types/${id}`, data);
}

/**
 * 删除字典类型
 */
export async function deleteDictType(id: number): Promise<void> {
  return request.delete<void>(`/api/v1/dicts/types/${id}`);
}

/**
 * 获取字典数据列表
 */
export async function getDictDataList(typeCode: string): Promise<DictDataItem[]> {
  return request.get<DictDataItem[]>(`/api/v1/dicts/data/${typeCode}`);
}

/**
 * 创建字典数据
 */
export async function createDictData(data: DictDataCreateRequest): Promise<number> {
  return request.post<number>('/api/v1/dicts/data', data);
}

/**
 * 更新字典数据
 */
export async function updateDictData(id: number, data: DictDataCreateRequest): Promise<void> {
  return request.put<void>(`/api/v1/dicts/data/${id}`, data);
}

/**
 * 删除字典数据
 */
export async function deleteDictData(id: number): Promise<void> {
  return request.delete<void>(`/api/v1/dicts/data/${id}`);
}

/**
 * 根据字典类型获取字典数据
 */
export async function getDictDataByType(typeCode: string): Promise<DictDataItem[]> {
  return getDictDataList(typeCode);
}

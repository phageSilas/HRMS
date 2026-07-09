/**
 * 个人中心相关接口
 * 负责人：成员 D
 */

import request from '@/utils/request';
import type { Result } from '@/types/api';

// ============ 类型定义 ============

export interface MyProfile {
  employeeId: number;
  employeeNo: string;
  name: string;
  gender: number;
  phone: string;
  email: string;
  departmentName: string;
  positionName: string;
  jobLevel: string;
  hireDate: string;
  employmentStatus: number;
  baseSalary: number;
}

export interface MyAttendance {
  yearMonth: string;
  workDays: number;
  actualWorkDays: number;
  leaveDays: number;
  lateCount: number;
  earlyLeaveCount: number;
}

export interface MySalary {
  yearMonth: string;
  baseSalary: number;
  bonus: number;
  deduction: number;
  actualSalary: number;
}

export interface MyApplication {
  id: number;
  bizType: string;
  title: string;
  status: number;
  createTime: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// ============ 个人信息接口 ============

/**
 * 获取我的档案
 */
export async function getMyProfile() {
  return request.get<Result<MyProfile>>('/my/profile');
}

/**
 * 获取我的考勤
 */
export async function getMyAttendance(yearMonth?: string) {
  return request.get<Result<MyAttendance[]>>('/my/attendance', { params: { yearMonth } });
}

/**
 * 获取我的薪资
 */
export async function getMySalary(yearMonth?: string) {
  return request.get<Result<MySalary[]>>('/my/salary', { params: { yearMonth } });
}

/**
 * 获取我的申请
 */
export async function getMyApplications() {
  return request.get<Result<MyApplication[]>>('/my/applications');
}

/**
 * 修改密码
 */
export async function changePassword(data: ChangePasswordRequest) {
  return request.put<Result<void>>('/my/password', data);
}
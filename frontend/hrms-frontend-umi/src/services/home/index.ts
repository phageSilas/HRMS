/**
 * 首页相关接口
 */

import request from '@/utils/request';

// ============ 类型定义 ============

export interface PendingCount {
  count: number;
  details: Array<{
    bizType: string;
    count: number;
  }>;
}

export interface AttendanceSummary {
  workDays: number;
  leaveDays: number;
  overtimeHours: number;
  lateTimes: number;
  earlyLeaveTimes: number;
}

export interface LeaveBalance {
  annualLeave: number;
  sickLeave: number;
  personalLeave: number;
}

export interface Application {
  id: number;
  type: string;
  submitTime: string;
  status: string;
  statusText: string;
  currentStep: string;
}

export interface PendingTask {
  id: number;
  bizType: string;
  applicant: string;
  deptName: string;
  submitTime: string;
  title: string;
}

// ============ 接口定义 ============

/**
 * 获取员工总数
 */
export async function getEmployeeCount(): Promise<{ count: number }> {
  return request.get('/employees/count');
}

/**
 * 获取本月入职人数
 */
export async function getMonthEntryCount(): Promise<{ count: number }> {
  return request.get('/employees/count/month-entry');
}

/**
 * 获取待审批数量
 */
export async function getPendingCount(): Promise<PendingCount> {
  return request.get('/approval/pending-count');
}

/**
 * 获取本月薪资总额
 */
export async function getMonthlySalaryTotal(): Promise<{ total: number }> {
  return request.get('/salary/monthly-total');
}

/**
 * 获取我的考勤汇总
 */
export async function getMyAttendanceSummary(): Promise<AttendanceSummary> {
  return request.get('/my/attendance/summary');
}

/**
 * 获取我的年假余额
 */
export async function getMyLeaveBalance(): Promise<LeaveBalance> {
  return request.get('/my/leave-balance');
}

/**
 * 获取我的申请列表
 */
export async function getMyApplications(): Promise<{ records: Application[]; total: number }> {
  return request.get('/my/applications');
}

/**
 * 获取待办任务列表
 */
export async function getPendingList(): Promise<{ records: PendingTask[]; total: number }> {
  return request.get('/approval/pending-list');
}
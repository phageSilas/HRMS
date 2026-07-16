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

/** 待办任务（与后端 PendingTaskVO 对齐） */
export interface PendingTask {
  id: number;           // 审批实例ID
  taskId: number;       // 审批任务ID（操作时使用）
  businessType: string; // 业务类型编码
  businessTypeName: string; // 业务类型名称
  title: string;        // 审批标题
  applicantName: string; // 申请人姓名
  nodeName: string;     // 当前审批节点名称
  createdAt: string;    // 申请时间
  deadline: string;     // 截止时间
  status: string;       // 状态编码
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
  return request.get('/api/v1/approval/pending-count');
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
 * 获取待办任务列表（调审批模块待办列表接口）
 */
export async function getPendingList(): Promise<{ records: PendingTask[]; total: number }> {
  return request.get('/api/v1/approval/tasks/pending');
}
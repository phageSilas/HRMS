/**
 * 考勤管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';
import type { Result, PageResult, PageQuery } from '@/types/api';

// ============ 类型定义 ============

export interface AttendanceRecord {
  id: number;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  date: string;
  clockInTime: string | null;
  clockOutTime: string | null;
  status: number;
  remark: string;
}

export interface LeaveRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: number;
  startTime: string;
  endTime: string;
  duration: number;
  reason: string;
  approvalStatus: number;
  approvalInstanceId: number | null;
  createTime: string;
}

export interface AttendanceSummary {
  employeeId: number;
  yearMonth: string;
  workDays: number;
  actualWorkDays: number;
  leaveDays: number;
  lateCount: number;
  earlyLeaveCount: number;
  absentDays: number;
  overtimeHours: number;
}

export interface AttendanceQuery extends PageQuery {
  employeeId?: number;
  departmentId?: number;
  startDate?: string;
  endDate?: string;
}

// ============ 考勤记录接口 ============

/**
 * 获取考勤记录列表
 */
export async function getAttendanceRecordList(params: AttendanceQuery) {
  return request.get<Result<PageResult<AttendanceRecord>>>('/attendance/records', { params });
}

/**
 * 打卡
 */
export async function clockIn(data: { type: 'in' | 'out' }) {
  return request.post<Result<AttendanceRecord>>('/attendance/records/clock', data);
}

// ============ 请假申请接口 ============

/**
 * 获取请假申请列表
 */
export async function getLeaveRequestList(params: AttendanceQuery) {
  return request.get<Result<PageResult<LeaveRequest>>>('/leave-requests', { params });
}

/**
 * 创建请假申请
 */
export async function createLeaveRequest(data: Partial<LeaveRequest>) {
  return request.post<Result<LeaveRequest>>('/leave-requests', data);
}

// ============ 考勤汇总接口 ============

/**
 * 获取员工考勤汇总（跨模块接口）
 */
export async function getAttendanceSummary(employeeId: number, yearMonth: string) {
  return request.get<Result<AttendanceSummary>>(`/attendance/summary/${employeeId}/${yearMonth}`);
}

/**
 * 获取考勤统计
 */
export async function getAttendanceStatistics(params: { yearMonth: string; departmentId?: number }) {
  return request.get<Result<AttendanceSummary[]>>('/attendance/statistics', { params });
}
/**
 * 个人中心（mycenter）前端接口服务
 * 对应后端 hrms-business-mycenter 模块全部 API
 */

import request from '@/utils/request';
import type { Result } from '@/types/api';

// ======================================================================
// 类型定义（与后端 DTO/VO 对齐）
// ======================================================================

/** 我的档案 VO（GET /api/v1/profile） */
export interface ProfileVO {
  employeeName: string;
  employeeNo: string;
  gender: string;
  birthDate: string;
  phone: string;
  email: string;
  idCard: string;
  emergencyContact: string;
  emergencyPhone: string;
  departmentName: string;
  positionName: string;
  hireDate: string;
  editableFields: string[];
  flowFields: string[];
}

/** 档案更新请求（只提交 editableFields 中的字段） */
export interface ProfileUpdateRequest {
  phone?: string;
  email?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
}

/** 考勤日历中的某一天 */
export interface AttendanceDayVO {
  date: string;
  status: string;       // NORMAL / LATE / EARLY_LEAVE / MISSED / LEAVE / HOLIDAY / ABSENT
  statusDesc: string;
  clockInTime?: string;
  clockOutTime?: string;
}

/** 考勤日历 VO */
export interface AttendanceCalendarVO {
  yearMonth: string;
  days: AttendanceDayVO[];
}

/** 打卡请求 */
export interface ClockInRequest {
  type: number; // 1=上班, 2=下班
}

/** 补卡申请请求 */
export interface MakeupRequest {
  correctionDate: string;
  correctionType: string;  // CLOCK_IN / CLOCK_OUT
  correctionReason: string;
}

/** 补卡记录 VO */
export interface MakeupRecordVO {
  id: number;
  correctionDate: string;
  correctionType: string;
  correctionReason: string;
  approvalStatus: number;
  createTime: string;
}

/** 请假请求 */
export interface LeaveRequestDTO {
  leaveType: string;
  startTime: string;
  endTime: string;
  totalDays: number;
  totalHours?: number;
  leaveReason: string;
  attachmentUrl?: string;
}

/** 请假列表 VO */
export interface LeaveVO {
  id: number;
  leaveType: string;
  leaveTypeDesc: string;
  startTime: string;
  endTime: string;
  totalDays: number;
  leaveReason: string;
  approvalStatus: number;
  approvalStatusDesc: string;
  createTime: string;
}

/** 假期余额 VO */
export interface LeaveBalanceVO {
  annualTotal: number;
  annualUsed: number;
  annualRemaining: number;
  compassionateTotal: number;
  compassionateUsed: number;
  compassionateRemaining: number;
}

/** 修改密码请求 */
export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
}

/** 绑定手机请求 */
export interface PhoneBindRequest {
  phone: string;
  smsCode: string;
}

/** 登录日志 VO */
export interface LoginLogVO {
  loginTime: string;
  ipAddress: string;
  deviceInfo: string;
}

// ======================================================================
// 档案模块 API
// ======================================================================

/** 获取我的档案 */
export async function getProfile() {
  return request.get<Result<ProfileVO>>('/api/v1/profile');
}

/** 更新我的档案 */
export async function updateProfile(data: ProfileUpdateRequest) {
  return request.put<Result<void>>('/api/v1/profile', data);
}

// ======================================================================
// 考勤模块 API
// ======================================================================

/** 获取考勤日历 */
export async function getAttendanceCalendar(yearMonth: string) {
  return request.get<Result<AttendanceCalendarVO>>('/api/v1/attendance/calendar', {
    params: { yearMonth },
  });
}

/** 打卡 */
export async function clockIn(data: ClockInRequest) {
  return request.post<Result<void>>('/api/v1/attendance/clock-in', data);
}

/** 申请补卡 */
export async function createMakeup(data: MakeupRequest) {
  return request.post<Result<void>>('/api/v1/attendance/makeup', data);
}

/** 补卡记录列表 */
export async function getMakeupRecords() {
  return request.get<Result<MakeupRecordVO[]>>('/api/v1/attendance/makeup/list');
}

// ======================================================================
// 请假模块 API
// ======================================================================

/** 提交请假申请 */
export async function createLeave(data: LeaveRequestDTO) {
  return request.post<Result<void>>('/api/v1/leave', data);
}

/** 查询请假记录 */
export async function getLeaveList() {
  return request.get<Result<LeaveVO[]>>('/api/v1/leave/list');
}

/** 取消请假 */
export async function cancelLeave(id: number) {
  return request.post<Result<void>>(`/api/v1/leave/${id}/cancel`);
}

/** 查询假期余额 */
export async function getLeaveBalance() {
  return request.get<Result<LeaveBalanceVO>>('/api/v1/leave/balance');
}

// ======================================================================
// 账号安全 API
// ======================================================================

/** 修改密码 */
export async function changePassword(data: PasswordChangeRequest) {
  return request.put<Result<void>>('/api/v1/account/password', data);
}

/** 绑定手机 */
export async function bindPhone(data: PhoneBindRequest) {
  return request.post<Result<void>>('/api/v1/account/phone/bind', data);
}

/** 获取登录日志 */
export async function getLoginLogs() {
  return request.get<Result<LoginLogVO[]>>('/api/v1/account/login-logs');
}

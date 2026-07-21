/**
 * 个人中心（mycenter）前端接口服务
 * 对应后端 hrms-business-mycenter 模块全部 API
 */

import request from '@/utils/request';

// ======================================================================
// 类型定义（与后端 DTO/VO 对齐）
// ======================================================================

/** 我的档案 VO（GET /api/v1/profile） */
export interface ProfileVO {
  employeeId?: number;
  employeeName: string;
  employeeNo: string;
  gender: number;
  genderDesc?: string;
  birthday: string;
  phone: string;
  email: string;
  idCard: string;
  emergencyContact: string;
  emergencyPhone: string;
  deptId?: number;
  deptName: string;
  postName: string;
  jobLevel?: string;
  leaderId?: number;
  hireDate: string;
  employmentStatus?: number;
  employmentStatusDesc?: string;
  currentAddress?: string;
  fieldPermissions?: {
    editableFields: string[];
    flowRequiredFields: string[];
    lockedFields: string[];
  };
}

/** 档案更新请求（只提交 editableFields 中的字段） */
export interface ProfileUpdateRequest {
  phone?: string;
  email?: string;
  currentAddress?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
}

/** 考勤状态枚举 */
export type AttendanceStatus =
  | 'NORMAL' | 'LATE' | 'EARLY_LEAVE'
  | 'MISSED' | 'LEAVE' | 'HOLIDAY' | 'ABSENT';

/** 考勤日历中的某一天 */
export interface AttendanceDayVO {
  date: string;
  status: string;
  statusDesc: string;
  clockInTime?: string;
  clockOutTime?: string;
  leaveType?: string;
  leaveTypeDesc?: string;
  correctionStatus?: string;
}

/** 考勤日历 VO */
export interface AttendanceCalendarVO {
  yearMonth: string;
  days: AttendanceDayVO[];
}

/** 打卡请求 */
export interface ClockInRequest {
  type: number;
}

/** 补卡申请请求 */
export interface MakeupRequest {
  correctionDate: string;
  correctionType: string;
  correctionReason: string;
}

/** 补卡记录 VO */
export interface MakeupRecordVO {
  id: number;
  correctionDate: string;
  correctionType: string;
  correctionReason: string;
  approvalStatus: number;
  approvalInstanceId?: number;
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
  approvalInstanceId?: number;
  createTime: string;
}

/** 假期余额 VO */
export interface LeaveBalanceVO {
  annualTotal: number;
  annualUsed: number;
  annualRemaining: number;
  sickTotal: number;
  sickUsed: number;
  sickRemaining: number;
  compassionateTotal: number;
  compassionateUsed: number;
  compassionateRemaining: number;
}

/** 修改密码请求 */
export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
}

/** 绑定/更换手机请求 */
export interface PhoneBindRequest {
  phone: string;
  password: string;
}

/** 登录日志 VO（与后端 LoginLogVO 对齐） */
export interface LoginLogVO {
  /** 登录IP */
  ip: string;
  /** 登录地点 */
  loginLocation?: string;
  /** 浏览器 */
  browser?: string;
  /** 操作系统 */
  os?: string;
  /** 登录状态：1-成功 0-失败 */
  status: number;
  /** 错误消息 */
  errorMsg?: string;
  /** 登录时间 */
  loginTime: string;
}

// ======================================================================
// 档案模块 API
// ======================================================================

/** 获取我的档案 */
export async function getProfile() {
  return request.get<ProfileVO>('/api/v1/profile');
}

/** 更新我的档案 */
export async function updateProfile(data: ProfileUpdateRequest) {
  return request.put<void>('/api/v1/profile', data);
}

// ======================================================================
// 考勤模块 API
// ======================================================================

/** 获取考勤日历 */
export async function getAttendanceCalendar(yearMonth: string) {
  return request.get<AttendanceCalendarVO>('/api/v1/attendance/calendar', {
    params: { yearMonth },
  });
}

/** 打卡 */
export async function clockIn(data: ClockInRequest) {
  return request.post<void>('/api/v1/attendance/clock-in', data);
}

/** 申请补卡 */
export async function createMakeup(data: MakeupRequest) {
  return request.post<void>('/api/v1/attendance/makeup', data);
}

/** 补卡记录列表 */
export async function getMakeupRecords() {
  return request.get<MakeupRecordVO[]>('/api/v1/attendance/makeup/list');
}

// ======================================================================
// 请假模块 API
// ======================================================================

/** 提交请假申请 */
export async function createLeave(data: LeaveRequestDTO) {
  return request.post<void>('/api/v1/leave', data);
}

/** 查询请假记录 */
export async function getLeaveList() {
  return request.get<LeaveVO[]>('/api/v1/leave/list');
}

/** 取消请假 */
export async function cancelLeave(id: number) {
  return request.post<void>(`/api/v1/leave/${id}/cancel`);
}

/** 查询假期余额 */
export async function getLeaveBalance() {
  return request.get<LeaveBalanceVO>('/api/v1/leave/balance');
}

// ======================================================================
// 加班模块 API
// ======================================================================

/** 加班申请请求 */
export interface OvertimeRequest {
  overtimeDate: string;
  duration: number;
  reason: string;
}

/** 加班记录 VO */
export interface OvertimeRecordVO {
  id: number;
  overtimeDate: string;
  duration: number;
  reason: string;
  approvalStatus: number;
  approvalStatusDesc: string;
  approvalInstanceId?: number;
  createTime: string;
}

/** 考勤统计数据映射（用于前端统计） */
export interface AttendanceStats {
  NORMAL: number;
  LATE: number;
  EARLY_LEAVE: number;
  MISSED: number;
  LEAVE: number;
  HOLIDAY: number;
  ABSENT: number;
}

/** 考勤统计 VO */
export interface AttendanceStatisticsVO {
  expectedDays: number;
  actualDays: number;
  lateCount: number;
  earlyLeaveCount: number;
  missCount: number;
  leaveCount: number;
}

/** 提交加班申请 */
export async function createOvertime(data: OvertimeRequest) {
  return request.post<void>('/api/v1/attendance/overtime', data);
}

/** 加班记录列表 */
export async function getOvertimeRecords() {
  return request.get<OvertimeRecordVO[]>('/api/v1/attendance/overtime');
}

/** 获取考勤统计 */
export async function getAttendanceStatistics(yearMonth: string) {
  return request.get<AttendanceStatisticsVO>('/api/v1/attendance/statistics', {
    params: { yearMonth },
  });
}

// ======================================================================
// 账号安全 API
// ======================================================================

/** 修改密码 */
export async function changePassword(data: PasswordChangeRequest) {
  return request.put<void>('/api/v1/account/password', data);
}

/** 绑定手机 */
export async function bindPhone(data: PhoneBindRequest) {
  return request.post<void>('/api/v1/account/phone/bind', data);
}

/** 解绑手机 */
export async function unbindPhone() {
  return request.post<void>('/api/v1/account/phone/unbind');
}

/** 获取登录日志 */
export async function getLoginLogs() {
  return request.get<LoginLogVO[]>('/api/v1/account/login-logs');
}

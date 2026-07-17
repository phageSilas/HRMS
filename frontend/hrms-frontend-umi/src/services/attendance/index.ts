/**
 * 考勤管理相关接口
 * 负责人：成员 C
 */

import request from '@/utils/request';
import type { PageQuery, PageResult } from '@/types/api';

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

export interface AttendanceTrendPoint {
  date: string | number[];
  attendanceRate: number | string;
  actualDays: number;
  expectedDays: number;
}

export interface AttendanceDeptDistribution {
  deptId: number;
  deptName: string;
  actualDays: number;
  expectedDays: number;
  attendanceRate: number | string;
}

export interface AttendanceExceptionPie {
  type: string;
  count: number;
}

export interface AttendanceEmployeeRanking {
  employeeId: number;
  employeeName: string;
  employeeNo: string;
  deptName: string;
  abnormalCount: number;
  lateCount: number;
  earlyLeaveCount: number;
  absentCount: number;
}

export interface AttendanceSummaryDashboard {
  expectedDays: number;
  actualDays: number;
  lateCount: number;
  earlyLeaveCount: number;
  absentCount: number;
  leaveCount: number | string;
  dailyTrend: AttendanceTrendPoint[];
  deptDistribution: AttendanceDeptDistribution[];
  exceptionPie: AttendanceExceptionPie[];
  employeeRanking: AttendanceEmployeeRanking[];
}

export interface AttendanceQuery extends PageQuery {
  employeeId?: number;
  departmentId?: number;
  startDate?: string;
  endDate?: string;
}

export interface AttendanceClockRequest {
  type?: 'CLOCK_IN' | 'CLOCK_OUT' | 'in' | 'out' | '1' | '2';
  latitude?: number;
  longitude?: number;
  deviceInfo?: string;
}

export interface AttendanceClockVO {
  recordId: number;
  employeeId: number;
  groupId?: number;
  recordDate: string | number[];
  period: 'CLOCK_IN' | 'CLOCK_OUT' | string;
  status: string;
  clockTime: string | number[];
  clockGps?: string;
  networkIp?: string;
  clientIp?: string;
}

export interface AttendanceCalendarDayVO {
  date: string | number[];
  clockInTime?: string | number[];
  clockOutTime?: string | number[];
  clockInGps?: string;
  clockOutGps?: string;
  clockInStatus?: string;
  clockOutStatus?: string;
  clockInIp?: string;
  clockOutIp?: string;
  dayStatus?: string;
  leave?: boolean;
}

export interface AttendanceCalendarVO {
  employeeId?: number;
  yearMonth: string;
  days: AttendanceCalendarDayVO[];
}

export interface AttendanceGroupQuery extends Partial<PageQuery> {
  groupName?: string;
  status?: number;
}

export interface AttendanceGroup {
  id: number;
  groupName: string;
  shiftType: 'FIXED' | 'FLEXIBLE' | 'SCHEDULED' | string;
  workStartTime?: string | number[];
  workEndTime?: string | number[];
  lateThresholdMinutes?: number;
  earlyLeaveThresholdMinutes?: number;
  monthlyCorrectionLimit?: number;
  status?: number;
  statusText?: string;
  createTime?: string | number[];
}

export interface AttendanceGroupRequest {
  groupName: string;
  shiftType: 'FIXED' | 'FLEXIBLE' | 'SCHEDULED' | string;
  clockInTime: string;
  clockOutTime: string;
  restStartTime?: string;
  restEndTime?: string;
  flexibleStartTime?: string;
  flexibleEndTime?: string;
  lateThreshold?: number;
  earlyLeaveThreshold?: number;
  maxCorrectionCount?: number;
  ipWhitelist?: string;
  locationRange?: {
    latitude?: number;
    longitude?: number;
    radius?: number;
    address?: string;
  };
  status?: number;
}

export interface AttendanceGroupRecordQuery extends Partial<PageQuery> {
  yearMonth?: string;
  dateStart?: string;
  dateEnd?: string;
  keyword?: string;
  departmentId?: number;
  status?: string;
}

export interface AttendanceGroupRecord {
  recordId: number;
  recordDate: string | number[];
  employeeId: number;
  employeeName: string;
  employeeNo?: string;
  deptId?: number;
  deptName?: string;
  clockInTime?: string | number[];
  clockOutTime?: string | number[];
  clockInStatus?: string;
  clockOutStatus?: string;
  status?: string;
  statusName?: string;
}

export interface AttendanceLeaveManageQuery extends Partial<PageQuery> {
  yearMonth?: string;
  deptId?: number;
  keyword?: string;
  approvalStatus?: number;
}

export interface AttendanceLeaveManageItem {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeNo?: string;
  deptId?: number;
  deptName?: string;
  leaveType?: string;
  leaveTypeDesc?: string;
  startTime?: string;
  endTime?: string;
  totalDays?: number | string;
  leaveReason?: string;
  approvalStatus?: number;
  approvalStatusDesc?: string;
  approvalInstanceId?: number;
  currentNodeName?: string;
  currentApproverName?: string;
  createTime?: string;
}

export interface AttendanceLeaveType {
  id: number;
  label: string;
  value: string;
}

export interface AttendanceLeaveCreateRequest {
  leaveTypeId?: number;
  leaveType?: string;
  startDate: string;
  startPeriod: 'AM' | 'PM';
  endDate: string;
  endPeriod: 'AM' | 'PM';
  reason?: string;
  attachmentFileId?: number;
  attachment?: string;
}

export interface AttendanceLeaveCreateResult {
  leaveId?: number;
  approvalInstanceId?: number;
  approvalStatus?: number;
}

// ============ 考勤组接口 ============

/**
 * 分页查询考勤组
 */
export async function getAttendanceGroups(params: AttendanceGroupQuery) {
  return request.get<PageResult<AttendanceGroup>>('/api/v1/attendance/groups', {
    params,
  });
}

/**
 * 创建考勤组
 */
export async function createAttendanceGroup(data: AttendanceGroupRequest) {
  return request.post<AttendanceGroup>('/api/v1/attendance/groups', data);
}

/**
 * 更新考勤组
 */
export async function updateAttendanceGroup(id: number, data: AttendanceGroupRequest) {
  return request.put<AttendanceGroup>(`/api/v1/attendance/groups/${id}`, data);
}

// ============ 考勤记录接口 ============

/**
 * 获取考勤记录列表
 */
export async function getAttendanceGroupRecords(
  groupId: number,
  params: AttendanceGroupRecordQuery,
) {
  return request.get<PageResult<AttendanceGroupRecord>>(
    `/api/v1/attendance/groups/${groupId}/records`,
    { params },
  );
}

export async function getAttendanceRecordList(params: AttendanceQuery) {
  return request.get<PageResult<AttendanceRecord>>('/attendance/records', { params });
}

/**
 * 打卡
 */
export async function clockIn(data: { type: 'in' | 'out' }) {
  return request.post<AttendanceRecord>('/attendance/records/clock', data);
}

/**
 * 当前登录员工打卡。
 */
export async function clockAttendance(data: AttendanceClockRequest) {
  return request.post<AttendanceClockVO>('/api/v1/attendance/clock', data);
}

/**
 * 查询当前登录员工个人月度打卡日历。
 */
export async function getMyAttendanceCalendar(yearMonth: string) {
  return request.get<AttendanceCalendarVO>('/api/v1/attendance/records/my-calendar', {
    params: { yearMonth },
  });
}

// ============ 请假申请接口 ============

/**
 * 获取请假申请列表
 */
export async function getLeaveRequestList(params: AttendanceQuery) {
  return request.get<PageResult<LeaveRequest>>('/leave-requests', { params });
}

/**
 * 创建请假申请
 */
export async function createLeaveRequest(data: Partial<LeaveRequest>) {
  return request.post<LeaveRequest>('/leave-requests', data);
}

/**
 * 获取管理侧请假列表
 */
export async function getAttendanceLeaveManageList(params: AttendanceLeaveManageQuery) {
  return request.get<PageResult<AttendanceLeaveManageItem>>('/api/v1/attendance/leaves', {
    params,
  });
}

/**
 * 获取启用的请假类型
 */
export async function getAttendanceLeaveTypes() {
  return request.get<AttendanceLeaveType[]>('/api/v1/leaves/types');
}

// ============ 考勤汇总接口 ============

/**
 * 获取员工考勤汇总（跨模块接口）
 */
/**
 * 提交请假申请
 */
export async function createAttendanceLeave(data: AttendanceLeaveCreateRequest) {
  return request.post<AttendanceLeaveCreateResult>('/api/v1/leaves', data);
}

export async function getAttendanceSummary(employeeId: number, yearMonth: string) {
  return request.get<AttendanceSummary>(`/attendance/summary/${employeeId}/${yearMonth}`);
}

/**
 * 获取考勤统计
 */
export async function getAttendanceStatistics(params: { yearMonth: string; departmentId?: number }) {
  return request.get<AttendanceSummary[]>('/attendance/statistics', { params });
}

/**
 * 获取HR和主管考勤统计看板。
 */
export async function getAttendanceSummaryDashboard(params: {
  yearMonth: string;
  deptId?: number;
}) {
  return request.get<AttendanceSummaryDashboard>('/api/v1/attendance/summary/dashboard', {
    params,
  });
}

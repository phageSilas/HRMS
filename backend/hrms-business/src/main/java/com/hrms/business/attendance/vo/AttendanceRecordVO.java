package com.hrms.business.attendance.vo;

import java.time.LocalDate;

/**
 * 考勤记录返回对象。
 *
 * @param id 考勤记录ID
 * @param employeeId 员工ID
 * @param attendanceDate 考勤日期
 * @param attendanceStatus 考勤状态
 */
public record AttendanceRecordVO(
    Long id,
    Long employeeId,
    LocalDate attendanceDate,
    String attendanceStatus
) {
}

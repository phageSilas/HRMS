package com.hrms.business.attendance.vo;

import lombok.Data;

import java.util.List;

/**
 * 个人月度打卡日历视图。
 */
@Data
public class AttendanceCalendarVO {

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 月份，格式 yyyy-MM。
     */
    private String yearMonth;

    /**
     * 每日考勤状态。
     */
    private List<AttendanceCalendarDayVO> days;
}

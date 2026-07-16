package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个人月度打卡日历视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

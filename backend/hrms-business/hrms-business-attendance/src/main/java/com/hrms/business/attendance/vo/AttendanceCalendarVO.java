package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
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
     * 当前生效考勤组ID。
     */
    private Long groupId;

    /**
     * 当前生效考勤组名称。
     */
    private String groupName;

    /**
     * 当前生效考勤组上班时间。
     */
    private LocalTime workStartTime;

    /**
     * 当前生效考勤组下班时间。
     */
    private LocalTime workEndTime;

    /**
     * 每日考勤状态。
     */
    private List<AttendanceCalendarDayVO> days;
}

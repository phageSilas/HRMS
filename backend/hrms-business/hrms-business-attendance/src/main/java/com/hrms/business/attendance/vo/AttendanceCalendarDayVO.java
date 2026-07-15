package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人打卡日历单日视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCalendarDayVO {

    /**
     * 日期。
     */
    private LocalDate date;

    /**
     * 上班打卡时间。
     */
    private LocalDateTime clockInTime;

    /**
     * 下班打卡时间。
     */
    private LocalDateTime clockOutTime;

    /**
     * 上班状态。
     */
    private String clockInStatus;

    /**
     * 下班状态。
     */
    private String clockOutStatus;

    /**
     * 当日综合状态。
     */
    private String dayStatus;

    /**
     * 是否请假。
     */
    private Boolean leave;
}

package com.hrms.business.attendance.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工打卡响应视图。
 */
@Data
public class AttendanceClockVO {

    /**
     * 打卡记录ID。
     */
    private Long recordId;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 考勤组ID。
     */
    private Long groupId;

    /**
     * 打卡日期。
     */
    private LocalDate recordDate;

    /**
     * 打卡时段。
     */
    private String period;

    /**
     * 打卡状态。
     */
    private String status;

    /**
     * 打卡时间。
     */
    private LocalDateTime clockTime;
}

package com.hrms.business.attendance.mq;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡成功领域事件。
 */
@Data
public class AttendanceClockCreatedEvent {

    /**
     * 消息唯一ID。
     */
    private String messageId;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 考勤组ID。
     */
    private Long groupId;

    /**
     * 打卡记录ID。
     */
    private Long recordId;

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

    /**
     * 设备信息。
     */
    private String deviceInfo;
}

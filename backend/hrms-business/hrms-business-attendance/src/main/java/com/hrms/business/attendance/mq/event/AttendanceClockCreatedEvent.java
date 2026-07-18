package com.hrms.business.attendance.mq.event;

import com.hrms.common.mq.HrmsMqMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡成功领域事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceClockCreatedEvent implements HrmsMqMessage {

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

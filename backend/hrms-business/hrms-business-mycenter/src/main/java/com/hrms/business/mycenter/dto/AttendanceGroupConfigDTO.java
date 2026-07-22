package com.hrms.business.mycenter.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 考勤组时间配置 DTO
 * <p>
 * 用于读取考勤组的上下班时间及迟到/早退阈值，
 * 在打卡时判定考勤状态（NORMAL / LATE / EARLY_LEAVE）。
 * </p>
 */
@Data
public class AttendanceGroupConfigDTO {

    /**
     * 上班时间
     */
    private LocalTime workStartTime;

    /**
     * 下班时间
     */
    private LocalTime workEndTime;

    /**
     * 迟到阈值（分钟），迟到时间超过该值判定为迟到
     */
    private Integer lateThresholdMinutes;

    /**
     * 早退阈值（分钟），早退时间超过该值判定为早退
     */
    private Integer earlyLeaveThresholdMinutes;
}

package com.hrms.business.attendance.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤组分页响应视图。
 */
@Data
public class AttendanceGroupPageVO {

    /**
     * 考勤组ID。
     */
    private Long id;

    /**
     * 考勤组名称。
     */
    private String groupName;

    /**
     * 班次类型。
     */
    private String shiftType;

    /**
     * 上班时间。
     */
    private LocalTime workStartTime;

    /**
     * 下班时间。
     */
    private LocalTime workEndTime;

    /**
     * 迟到阈值，单位分钟。
     */
    private Integer lateThresholdMinutes;

    /**
     * 早退阈值，单位分钟。
     */
    private Integer earlyLeaveThresholdMinutes;

    /**
     * 月补卡次数上限。
     */
    private Integer monthlyCorrectionLimit;

    /**
     * 状态：1-启用，0-禁用。
     */
    private Integer status;

    /**
     * 状态文案。
     */
    private String statusText;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}

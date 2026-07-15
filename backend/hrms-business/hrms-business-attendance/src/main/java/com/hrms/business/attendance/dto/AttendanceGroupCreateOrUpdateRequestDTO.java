package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * 考勤组创建或更新请求参数。
 */
@Data
public class AttendanceGroupCreateOrUpdateRequestDTO {

    /**
     * 考勤组名称。
     */
    @NotBlank(message = "考勤组名称不能为空")
    private String groupName;

    /**
     * 班次类型，兼容 fixed/flexible/scheduled 与 FIXED/FLEXIBLE/SCHEDULED。
     */
    @NotBlank(message = "班次类型不能为空")
    private String shiftType;

    /**
     * 上班时间。
     */
    @NotNull(message = "上班时间不能为空")
    private LocalTime clockInTime;

    /**
     * 下班时间。
     */
    @NotNull(message = "下班时间不能为空")
    private LocalTime clockOutTime;

    /**
     * 午休开始时间。
     */
    private LocalTime restStartTime;

    /**
     * 午休结束时间。
     */
    private LocalTime restEndTime;

    /**
     * 弹性最早打卡时间。
     */
    private LocalTime flexibleStartTime;

    /**
     * 弹性最晚打卡时间。
     */
    private LocalTime flexibleEndTime;

    /**
     * 迟到阈值，单位分钟。
     */
    @Min(value = 0, message = "迟到阈值不能小于0")
    private Integer lateThreshold;

    /**
     * 早退阈值，单位分钟。
     */
    @Min(value = 0, message = "早退阈值不能小于0")
    private Integer earlyLeaveThreshold;

    /**
     * 月补卡次数上限。
     */
    @Min(value = 0, message = "月补卡次数上限不能小于0")
    private Integer maxCorrectionCount;

    /**
     * GPS 打卡范围，允许前端传对象或字符串，落库时序列化。
     */
    private Object locationRange;

    /**
     * 成员范围，当前库无考勤组成员表，本字段暂不落库。
     */
    private Object memberRange;

    /**
     * IP 白名单，逗号分隔或 JSON 字符串。
     */
    private String ipWhitelist;

    /**
     * 状态：1-启用，0-禁用。
     */
    private Integer status;
}

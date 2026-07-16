package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 考勤组实体，对应 hr_attendance_group。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_group")
public class AttendanceGroupEntity extends BaseEntity {

    /**
     * 考勤组名称。
     */
    private String groupName;

    /**
     * 班次类型：FIXED/FLEXIBLE/SCHEDULED。
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
    private Integer lateThresholdMinutes;

    /**
     * 早退阈值，单位分钟。
     */
    private Integer earlyLeaveThresholdMinutes;

    /**
     * 打卡 IP 白名单，逗号分隔。
     */
    private String clockIpWhitelist;

    /**
     * GPS 打卡范围配置。
     */
    private String clockGpsScope;

    /**
     * 月补卡次数上限。
     */
    private Integer monthlyCorrectionLimit;

    /**
     * 状态：1-启用，0-禁用。
     */
    private Integer status;
}

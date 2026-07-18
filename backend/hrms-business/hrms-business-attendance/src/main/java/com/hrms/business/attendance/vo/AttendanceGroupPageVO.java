package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 考勤组分页响应视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 适用范围类型：DEPT/POST/EMPLOYEE。
     */
    private String scopeType;

    /**
     * 适用范围原始值。
     */
    private String scopeValue;

    /**
     * 适用范围展示名称。
     */
    private String scopeName;

    /**
     * 当前成员人数。
     */
    private Integer memberCount;

    /**
     * 部门范围 ID 列表。
     */
    private List<Long> deptIds;

    /**
     * 指定员工模式下的部门 ID。
     */
    private Long deptId;

    /**
     * 职位范围 ID。
     */
    private Long postId;

    /**
     * 指定员工 ID 列表。
     */
    private List<Long> employeeIds;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}

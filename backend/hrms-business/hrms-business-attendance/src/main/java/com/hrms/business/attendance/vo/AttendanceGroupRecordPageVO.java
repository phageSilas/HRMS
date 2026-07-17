package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤组打卡记录分页视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGroupRecordPageVO {

    /**
     * 打卡记录ID。
     */
    private Long recordId;

    /**
     * 打卡日期。
     */
    private LocalDate recordDate;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 员工工号。
     */
    private String employeeNo;

    /**
     * 部门ID。
     */
    private Long deptId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 上班打卡时间。
     */
    private LocalTime clockInTime;

    /**
     * 下班打卡时间。
     */
    private LocalTime clockOutTime;

    /**
     * 上班状态。
     */
    private String clockInStatus;

    /**
     * 下班状态。
     */
    private String clockOutStatus;

    /**
     * 综合打卡状态。
     */
    private String status;

    /**
     * 综合打卡状态名称。
     */
    private String statusName;
}

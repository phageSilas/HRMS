package com.hrms.business.attendance.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资模块读取的月度考勤汇总视图。
 */
@Data
public class AttendancePayrollSourceVO {

    private Long employeeId;

    private String employeeNo;

    private String employeeName;

    private Integer shouldAttendDays;

    private Integer actualAttendDays;

    private Integer lateCount;

    private Integer earlyLeaveCount;

    private BigDecimal absenceDays;

    private BigDecimal leaveDays;

    private BigDecimal overtimeHours;
}

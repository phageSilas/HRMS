package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资模块读取的月度考勤汇总视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

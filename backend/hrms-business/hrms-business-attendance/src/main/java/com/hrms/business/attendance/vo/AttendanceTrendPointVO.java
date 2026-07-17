package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 考勤统计每日趋势视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceTrendPointVO {

    /**
     * 日期。
     */
    private LocalDate date;

    /**
     * 出勤率。
     */
    private BigDecimal attendanceRate;

    /**
     * 实际出勤人天。
     */
    private Integer actualDays;

    /**
     * 应出勤人天。
     */
    private Integer expectedDays;
}

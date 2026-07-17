package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考勤统计看板聚合视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDashboardVO {

    /**
     * 应出勤总人天。
     */
    private Integer expectedDays;

    /**
     * 实际出勤总人天。
     */
    private Integer actualDays;

    /**
     * 迟到次数。
     */
    private Integer lateCount;

    /**
     * 早退次数。
     */
    private Integer earlyLeaveCount;

    /**
     * 缺勤次数。
     */
    private Integer absentCount;

    /**
     * 请假天数。
     */
    private BigDecimal leaveCount;

    /**
     * 每日趋势。
     */
    private List<AttendanceTrendPointVO> dailyTrend;

    /**
     * 部门分布。
     */
    private List<AttendanceDeptDistributionVO> deptDistribution;

    /**
     * 异常占比。
     */
    private List<AttendanceExceptionPieVO> exceptionPie;

    /**
     * 员工异常排行。
     */
    private List<AttendanceEmployeeRankingVO> employeeRanking;
}

package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 考勤统计部门分布视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDeptDistributionVO {

    /**
     * 部门ID。
     */
    private Long deptId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 实际出勤人天。
     */
    private Integer actualDays;

    /**
     * 应出勤人天。
     */
    private Integer expectedDays;

    /**
     * 出勤率。
     */
    private BigDecimal attendanceRate;
}

package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤异常员工排行视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEmployeeRankingVO {

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
     * 部门名称。
     */
    private String deptName;

    /**
     * 异常总次数。
     */
    private Integer abnormalCount;

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
}

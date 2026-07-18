package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤异常占比视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExceptionPieVO {

    /**
     * 异常类型。
     */
    private String type;

    /**
     * 异常数量。
     */
    private Integer count;
}

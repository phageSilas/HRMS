package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤统计")
public class AttendanceStatisticsVO {

    @Schema(description = "应出勤天数")
    private Integer expectedDays;

    @Schema(description = "实际出勤天数")
    private Integer actualDays;

    @Schema(description = "迟到次数")
    private Integer lateCount;

    @Schema(description = "早退次数")
    private Integer earlyLeaveCount;

    @Schema(description = "缺卡次数")
    private Integer missCount;

    @Schema(description = "请假天数")
    private Integer leaveCount;
}

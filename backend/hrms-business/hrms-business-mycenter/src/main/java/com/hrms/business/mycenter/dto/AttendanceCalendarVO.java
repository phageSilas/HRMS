package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 考勤日历 VO
 */
@Data
@Schema(description = "考勤日历")
public class AttendanceCalendarVO {

    @Schema(description = "年月 yyyy-MM")
    private String yearMonth;

    @Schema(description = "每日考勤明细")
    private List<AttendanceDayVO> days;

    @Data
    @Schema(description = "单日考勤")
    public static class AttendanceDayVO {

        @Schema(description = "日期 yyyy-MM-dd")
        private String date;

        @Schema(description = "考勤状态：NORMAL/LATE/EARLY_LEAVE/ABSENT/MISSED/LEAVE/HOLIDAY")
        private String status;

        @Schema(description = "状态描述")
        private String statusDesc;

        @Schema(description = "上班打卡时间 HH:mm")
        private String clockInTime;

        @Schema(description = "下班打卡时间 HH:mm")
        private String clockOutTime;
    }
}

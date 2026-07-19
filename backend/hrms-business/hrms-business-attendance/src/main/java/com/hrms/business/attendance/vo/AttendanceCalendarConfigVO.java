package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤日历配置响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCalendarConfigVO {

    /**
     * 配置年份。
     */
    private Integer year;

    /**
     * 工作日列表，使用 1~7 表示周一到周日。
     */
    private List<Integer> workdays;

    /**
     * 法定节假日日期列表。
     */
    private List<LocalDate> holidayDates;
}

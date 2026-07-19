package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤日历配置保存请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCalendarConfigRequestDTO {

    /**
     * 配置年份。
     */
    @NotNull(message = "配置年份不能为空")
    @Min(value = 2000, message = "配置年份不合法")
    @Max(value = 2100, message = "配置年份不合法")
    private Integer year;

    /**
     * 工作日列表，使用 1~7 表示周一到周日。
     */
    @NotEmpty(message = "至少选择一个工作日")
    private List<Integer> workdays;

    /**
     * 法定节假日日期列表。
     */
    private List<LocalDate> holidayDates;
}

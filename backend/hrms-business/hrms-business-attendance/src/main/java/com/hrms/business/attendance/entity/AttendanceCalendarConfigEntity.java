package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考勤日历配置实体，对应 hr_attendance_calendar_config。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_calendar_config")
public class AttendanceCalendarConfigEntity extends BaseEntity {

    /**
     * 配置年份。
     */
    private Integer configYear;

    /**
     * 工作日配置 JSON，使用 1~7 表示周一到周日。
     */
    private String workdaysJson;

    /**
     * 法定节假日日期 JSON，格式为 yyyy-MM-dd。
     */
    private String holidayDatesJson;

    /**
     * 备注。
     */
    private String remark;
}

package com.hrms.business.attendance.convert;

import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;

/**
 * 个人月历考勤组摘要补齐器。
 */
public final class AttendanceCalendarSummaryEnricher {

    private AttendanceCalendarSummaryEnricher() {
    }

    /**
     * 将当前生效考勤组摘要写入个人月历 VO。
     *
     * @param calendar 月历 VO
     * @param group    当前生效考勤组，可为空
     * @return 补齐后的月历 VO
     * 本方法使用的工具类: 无
     */
    public static AttendanceCalendarVO enrich(AttendanceCalendarVO calendar, AttendanceGroupEntity group) {
        if (calendar == null) {
            return null;
        }
        if (group == null) {
            calendar.setGroupId(null);
            calendar.setGroupName(null);
            calendar.setWorkStartTime(null);
            calendar.setWorkEndTime(null);
            return calendar;
        }
        calendar.setGroupId(group.getId());
        calendar.setGroupName(group.getGroupName());
        calendar.setWorkStartTime(group.getWorkStartTime());
        calendar.setWorkEndTime(group.getWorkEndTime());
        return calendar;
    }
}

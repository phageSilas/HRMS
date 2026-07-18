package com.hrms.business.attendance.convert;

import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 个人月历考勤组摘要补齐器测试。
 */
class AttendanceCalendarSummaryEnricherTest {

    /**
     * 验证有生效考勤组时，会把组名和上下班时间写入月历摘要。
     *
     * 本方法使用的工具类: LocalTime(JDK),List(JDK)
     */
    @Test
    void shouldEnrichCalendarWithAttendanceGroupSummary() {
        AttendanceCalendarVO calendar = AttendanceCalendarVO.builder()
                .employeeId(9001L)
                .yearMonth("2026-07")
                .days(List.of())
                .build();
        AttendanceGroupEntity group = new AttendanceGroupEntity();
        group.setId(3001L);
        group.setGroupName("标准工时组");
        group.setWorkStartTime(LocalTime.of(9, 0));
        group.setWorkEndTime(LocalTime.of(18, 0));

        AttendanceCalendarVO result = AttendanceCalendarSummaryEnricher.enrich(calendar, group);

        assertEquals(3001L, result.getGroupId());
        assertEquals("标准工时组", result.getGroupName());
        assertEquals(LocalTime.of(9, 0), result.getWorkStartTime());
        assertEquals(LocalTime.of(18, 0), result.getWorkEndTime());
    }

    /**
     * 验证未配置有效考勤组时，月历摘要字段保持为空。
     *
     * 本方法使用的工具类: List(JDK)
     */
    @Test
    void shouldKeepCalendarSummaryEmptyWhenAttendanceGroupMissing() {
        AttendanceCalendarVO calendar = AttendanceCalendarVO.builder()
                .employeeId(9002L)
                .yearMonth("2026-07")
                .days(List.of())
                .build();

        AttendanceCalendarVO result = AttendanceCalendarSummaryEnricher.enrich(calendar, null);

        assertNull(result.getGroupId());
        assertNull(result.getGroupName());
        assertNull(result.getWorkStartTime());
        assertNull(result.getWorkEndTime());
    }
}

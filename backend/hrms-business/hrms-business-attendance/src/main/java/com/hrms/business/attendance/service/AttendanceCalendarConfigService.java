package com.hrms.business.attendance.service;

import com.hrms.business.attendance.dto.AttendanceCalendarConfigRequestDTO;
import com.hrms.business.attendance.vo.AttendanceCalendarConfigVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤日历配置服务。
 */
public interface AttendanceCalendarConfigService {

    /**
     * 查询指定年份的考勤日历配置。
     *
     * @param year 配置年份
     * @return 日历配置
     */
    AttendanceCalendarConfigVO getCalendarConfig(Integer year);

    /**
     * 保存指定年份的考勤日历配置。
     *
     * @param requestDTO 配置请求
     * @return 保存后的日历配置
     */
    AttendanceCalendarConfigVO saveCalendarConfig(AttendanceCalendarConfigRequestDTO requestDTO);

    /**
     * 判断某一天是否为工作日。
     *
     * @param date 日期
     * @return 是否为工作日
     */
    boolean isWorkday(LocalDate date);

    /**
     * 查询日期范围内的工作日列表。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作日列表
     */
    List<LocalDate> listWorkdays(LocalDate startDate, LocalDate endDate);

    /**
     * 统计日期范围内的工作日数量。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作日数量
     */
    int countWorkdays(LocalDate startDate, LocalDate endDate);
}

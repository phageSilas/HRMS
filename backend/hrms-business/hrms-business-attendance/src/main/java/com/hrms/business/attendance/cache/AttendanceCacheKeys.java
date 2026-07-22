package com.hrms.business.attendance.cache;

import java.time.LocalDate;

/**
 * 考勤模块 Redis Key 常量。
 */
public final class AttendanceCacheKeys {

    private AttendanceCacheKeys() {
    }

    /**
     * 考勤组规则缓存 Key 前缀。
     */
    public static final String GROUP_RULE_PREFIX = "attendance:group:rule:";

    /**
     * 考勤组分页缓存 Key 前缀。
     */
    public static final String GROUP_PAGE_PREFIX = "attendance:group:page:";

    /**
     * 构建考勤组规则缓存 Key。
     *
     * @param groupId 考勤组ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String groupRule(Long groupId) {
        return GROUP_RULE_PREFIX + groupId;
    }

    /**
     * 构建考勤组分页缓存 Key。
     *
     * @param queryKey 查询条件唯一键
     * @return Redis Key
     */
    public static String attendanceGroupPage(String queryKey) {
        return GROUP_PAGE_PREFIX + queryKey;
    }

    /**
     * 构建考勤组分页缓存 Key 模式。
     *
     * @return Redis Key 模式
     */
    public static String attendanceGroupPagePattern() {
        return GROUP_PAGE_PREFIX + "*";
    }

    /**
     * 构建员工每日打卡缓存 Key。
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String dailyRecord(Long employeeId, LocalDate date) {
        return "attendance:record:daily:" + employeeId + ":" + date;
    }

    /**
     * 构建员工月度日历缓存 Key。
     *
     * @param employeeId 员工ID
     * @param yearMonth  月份，格式 yyyy-MM
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String monthCalendar(Long employeeId, String yearMonth) {
        return "attendance:calendar:month:" + employeeId + ":" + yearMonth;
    }

    /**
     * 构建打卡消息幂等 Key。
     *
     * @param messageId 消息ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String clockMessageIdempotent(String messageId) {
        return "attendance:mq:clock:created:" + messageId;
    }

    /**
     * 构建月度统计生成锁 Key。
     *
     * @param month 月份
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String monthStatGenerateLock(String month) {
        return "attendance:stat:generate:lock:" + month;
    }

    /**
     * 构建员工月度统计缓存 Key。
     *
     * @param employeeId 员工ID
     * @param month      月份，格式 yyyyMM
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String monthStat(Long employeeId, String month) {
        return "attendance:month:stat:" + employeeId + ":" + month.replace("-", "");
    }
}

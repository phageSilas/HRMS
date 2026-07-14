package com.hrms.business.attendance.cache;

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
     * 构建考勤组规则缓存 Key。
     *
     * @param groupId 考勤组ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String groupRule(Long groupId) {
        return GROUP_RULE_PREFIX + groupId;
    }
}

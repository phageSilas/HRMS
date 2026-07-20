package com.hrms.business.attendance.enums;

/**
 * 打卡时段枚举。
 */
public enum ClockPeriodEnum {

    /**
     * 上班打卡。
     */
    CLOCK_IN,

    /**
     * 下班打卡。
     */
    CLOCK_OUT;

    /**
     * 根据前端或后端入参解析打卡时段。
     *
     * @param type 打卡类型
     * @return 打卡时段，无法解析时返回 null
     * 本方法使用的工具类: 无
     */
    public static ClockPeriodEnum parse(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String normalized = type.trim().toUpperCase();
        return switch (normalized) {
            case "IN", "CLOCK_IN", "CHECK_IN", "ON", "1" -> CLOCK_IN;
            case "OUT", "CLOCK_OUT", "CHECK_OUT", "OFF", "2" -> CLOCK_OUT;
            default -> null;
        };
    }
}

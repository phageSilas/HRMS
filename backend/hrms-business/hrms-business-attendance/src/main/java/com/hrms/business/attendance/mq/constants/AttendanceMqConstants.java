package com.hrms.business.attendance.mq.constants;

/**
 * 考勤模块 RabbitMQ 常量。
 */
public final class AttendanceMqConstants {

    private AttendanceMqConstants() {
    }

    /**
     * 考勤事件交换机。
     */
    public static final String EXCHANGE = "hrms.attendance.exchange";

    /**
     * 打卡成功事件 routing key。
     */
    public static final String CLOCK_CREATED_ROUTING_KEY = "attendance.clock.created";

    /**
     * 打卡成功事件队列。
     */
    public static final String CLOCK_CREATED_QUEUE = "hrms.attendance.clock.created.queue";

    /**
     * 月度统计生成事件 routing key。
     */
    public static final String MONTHLY_STAT_GENERATE_ROUTING_KEY = "attendance.stat.monthly.generate";

    /**
     * 月度统计生成事件队列。
     */
    public static final String MONTHLY_STAT_GENERATE_QUEUE = "hrms.attendance.stat.monthly.generate.queue";
}

package com.hrms.business.attendance.mq;

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
}

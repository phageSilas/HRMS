package com.hrms.business.attendance.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 考勤模块 RabbitMQ 交换机、队列和绑定配置。
 */
@Configuration
public class AttendanceRabbitMqConfig {

    /**
     * 声明考勤模块交换机。
     *
     * @return 考勤模块交换机
     * 本方法使用的工具类: DirectExchange(spring-amqp)
     */
    @Bean
    public DirectExchange attendanceExchange() {
        return new DirectExchange(AttendanceMqConstants.EXCHANGE, true, false);
    }

    /**
     * 声明打卡成功事件队列。
     *
     * @return 打卡成功事件队列
     * 本方法使用的工具类: Queue(spring-amqp)
     */
    @Bean
    public Queue attendanceClockCreatedQueue() {
        return new Queue(AttendanceMqConstants.CLOCK_CREATED_QUEUE, true);
    }

    /**
     * 绑定打卡成功事件队列。
     *
     * @param attendanceExchange 考勤模块交换机
     * @param attendanceClockCreatedQueue 打卡成功事件队列
     * @return 队列绑定关系
     * 本方法使用的工具类: BindingBuilder(spring-amqp)
     */
    @Bean
    public Binding attendanceClockCreatedBinding(DirectExchange attendanceExchange,
                                                 @Qualifier("attendanceClockCreatedQueue")
                                                 Queue attendanceClockCreatedQueue) {
        return BindingBuilder.bind(attendanceClockCreatedQueue)
                .to(attendanceExchange)
                .with(AttendanceMqConstants.CLOCK_CREATED_ROUTING_KEY);
    }

    /**
     * 声明月度统计生成事件队列。
     *
     * @return 月度统计生成事件队列
     * 本方法使用的工具类: Queue(spring-amqp)
     */
    @Bean
    public Queue attendanceMonthlyStatGenerateQueue() {
        return new Queue(AttendanceMqConstants.MONTHLY_STAT_GENERATE_QUEUE, true);
    }

    /**
     * 绑定月度统计生成事件队列。
     *
     * @param attendanceExchange 考勤模块交换机
     * @param attendanceMonthlyStatGenerateQueue 月度统计生成事件队列
     * @return 队列绑定关系
     * 本方法使用的工具类: BindingBuilder(spring-amqp)
     */
    @Bean
    public Binding attendanceMonthlyStatGenerateBinding(DirectExchange attendanceExchange,
                                                       @Qualifier("attendanceMonthlyStatGenerateQueue")
                                                       Queue attendanceMonthlyStatGenerateQueue) {
        return BindingBuilder.bind(attendanceMonthlyStatGenerateQueue)
                .to(attendanceExchange)
                .with(AttendanceMqConstants.MONTHLY_STAT_GENERATE_ROUTING_KEY);
    }

}

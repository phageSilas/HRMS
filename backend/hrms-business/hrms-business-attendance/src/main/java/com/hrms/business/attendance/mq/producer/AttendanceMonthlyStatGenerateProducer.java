package com.hrms.business.attendance.mq.producer;

import com.hrms.business.attendance.mq.constants.AttendanceMqConstants;
import com.hrms.business.attendance.mq.event.AttendanceMonthlyStatGenerateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 考勤月度统计生成消息生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceMonthlyStatGenerateProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送月度统计生成消息。
     *
     * @param message 月度统计生成消息
     * 本方法使用的工具类: RabbitTemplate(spring-amqp)
     */
    public void send(AttendanceMonthlyStatGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                AttendanceMqConstants.EXCHANGE,
                AttendanceMqConstants.MONTHLY_STAT_GENERATE_ROUTING_KEY,
                message);
        log.info("send attendance.stat.monthly.generate success, messageId={}, month={}",
                message.getMessageId(), message.getMonth());
    }
}

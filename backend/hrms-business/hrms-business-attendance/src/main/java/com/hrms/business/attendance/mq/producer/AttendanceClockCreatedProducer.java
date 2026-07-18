package com.hrms.business.attendance.mq.producer;

import com.hrms.business.attendance.mq.constants.AttendanceMqConstants;
import com.hrms.business.attendance.mq.event.AttendanceClockCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 考勤打卡成功事件生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceClockCreatedProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: RabbitTemplate(spring-amqp)
     */
    public void send(AttendanceClockCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                AttendanceMqConstants.EXCHANGE,
                AttendanceMqConstants.CLOCK_CREATED_ROUTING_KEY,
                event);
        log.info("send attendance.clock.created success, messageId={}, recordId={}",
                event.getMessageId(), event.getRecordId());
    }
}

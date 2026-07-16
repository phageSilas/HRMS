package com.hrms.business.salary.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 薪资批次核算消息生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalaryBatchCalculateProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送薪资批次核算消息。
     *
     * @param message 薪资批次核算消息
     * 本方法使用的工具类: RabbitTemplate(spring-amqp)
     */
    public void send(SalaryBatchCalculateMessage message) {
        rabbitTemplate.convertAndSend(
                SalaryMqConstants.SALARY_EXCHANGE,
                SalaryMqConstants.BATCH_CALCULATE_ROUTING_KEY,
                message);
        log.info("send salary.batch.calculate success, messageId={}, batchId={}",
                message.getMessageId(), message.getBatchId());
    }
}

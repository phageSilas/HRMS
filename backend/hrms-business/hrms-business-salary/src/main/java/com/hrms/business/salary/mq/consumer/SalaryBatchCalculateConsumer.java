package com.hrms.business.salary.mq.consumer;

import com.hrms.business.salary.mq.event.SalaryBatchCalculateMessage;
import com.hrms.business.salary.mq.constants.SalaryMqConstants;
import com.hrms.business.salary.service.SalaryCalculateService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.mq.MessageQueueIdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 薪资批次核算消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalaryBatchCalculateConsumer {

    private final SalaryCalculateService salaryCalculateService;

    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    /**
     * 消费薪资批次核算消息。
     *
     * @param message 薪资批次核算消息
     * 本方法使用的工具类: RabbitListener(spring-amqp),MessageQueueIdempotentHandler(hrms-common)
     */
    @RabbitListener(queues = SalaryMqConstants.BATCH_CALCULATE_QUEUE)
    public void onMessage(SalaryBatchCalculateMessage message) {
        String messageId = message.getMessageId();
        if (messageQueueIdempotentHandler.isMessageBeingConsumed(messageId)) {
            if (messageQueueIdempotentHandler.isAccomplish(messageId)) {
                return;
            }
            throw new GlobalException(ErrorCode.CONFLICT, "薪资批次核算消息正在消费中，等待 RabbitMQ 重试");
        }
        try {
            salaryCalculateService.handleBatchCalculateMessage(message);
            messageQueueIdempotentHandler.setAccomplish(messageId);
            log.info("consume salary.batch.calculate success, messageId={}, batchId={}",
                    messageId, message.getBatchId());
        } catch (Exception ex) {
            messageQueueIdempotentHandler.delMessageProcessed(messageId);
            log.error("consume salary.batch.calculate failed, messageId={}", messageId, ex);
            throw ex;
        }
    }
}

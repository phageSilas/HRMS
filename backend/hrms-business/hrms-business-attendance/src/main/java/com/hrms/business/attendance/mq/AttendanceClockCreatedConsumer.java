package com.hrms.business.attendance.mq;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.mq.MessageQueueIdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 考勤打卡成功事件消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceClockCreatedConsumer {

    private final AttendanceClockEventHandler attendanceClockEventHandler;

    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    /**
     * 消费打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: RabbitListener(spring-amqp),MessageQueueIdempotentHandler(hrms-common)
     */
    @RabbitListener(queues = AttendanceMqConstants.CLOCK_CREATED_QUEUE)
    public void onMessage(AttendanceClockCreatedEvent event) {
        String messageId = event.getMessageId();
        if (messageQueueIdempotentHandler.isMessageBeingConsumed(messageId)) {
            if (messageQueueIdempotentHandler.isAccomplish(messageId)) {
                return;
            }
            throw new GlobalException(ErrorCode.CONFLICT, "打卡成功事件正在消费中，等待 RabbitMQ 重试");
        }
        try {
            attendanceClockEventHandler.handleClockCreatedEvent(event);
            messageQueueIdempotentHandler.setAccomplish(messageId);
            log.info("consume attendance.clock.created success, messageId={}, recordId={}",
                    messageId, event.getRecordId());
        } catch (Exception ex) {
            messageQueueIdempotentHandler.delMessageProcessed(messageId);
            log.error("consume attendance.clock.created failed, messageId={}", messageId, ex);
            throw ex;
        }
    }
}

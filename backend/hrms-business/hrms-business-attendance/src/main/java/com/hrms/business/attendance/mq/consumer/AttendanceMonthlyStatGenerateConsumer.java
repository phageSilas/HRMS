package com.hrms.business.attendance.mq.consumer;

import com.hrms.business.attendance.mq.event.AttendanceMonthlyStatGenerateMessage;
import com.hrms.business.attendance.mq.constants.AttendanceMqConstants;
import com.hrms.business.attendance.service.impl.AttendanceServiceImpl;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.mq.MessageQueueIdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 考勤月度统计生成消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceMonthlyStatGenerateConsumer {

    private final AttendanceServiceImpl attendanceService;

    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    /**
     * 消费月度统计生成消息。
     *
     * @param message 月度统计生成消息
     * 本方法使用的工具类: RabbitListener(spring-amqp),MessageQueueIdempotentHandler(hrms-common)
     */
    @RabbitListener(queues = AttendanceMqConstants.MONTHLY_STAT_GENERATE_QUEUE)
    public void onMessage(AttendanceMonthlyStatGenerateMessage message) {
        String messageId = message.getMessageId();
        if (messageQueueIdempotentHandler.isMessageBeingConsumed(messageId)) {
            if (messageQueueIdempotentHandler.isAccomplish(messageId)) {
                return;
            }
            throw new GlobalException(ErrorCode.CONFLICT, "考勤月度统计消息正在消费中，等待 RabbitMQ 重试");
        }
        try {
            attendanceService.handleMonthlyStatGenerateMessage(message);
            messageQueueIdempotentHandler.setAccomplish(messageId);
            log.info("consume attendance.stat.monthly.generate success, messageId={}, month={}",
                    messageId, message.getMonth());
        } catch (Exception ex) {
            messageQueueIdempotentHandler.delMessageProcessed(messageId);
            log.error("consume attendance.stat.monthly.generate failed, messageId={}", messageId, ex);
            throw ex;
        }
    }
}

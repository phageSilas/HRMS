package com.hrms.common.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * RabbitMQ 消息幂等处理器，基于 Redis 标记消息处理中和已完成状态。
 */
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {

    private static final String IDEMPOTENT_KEY_PREFIX = "hrms:mq:idempotent:";

    private static final String PROCESSING_STATUS = "0";

    private static final String ACCOMPLISH_STATUS = "1";

    private static final Duration PROCESSING_TTL = Duration.ofMinutes(10);

    private static final Duration ACCOMPLISH_TTL = Duration.ofHours(2);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 判断消息是否已经处于消费流程中。
     *
     * @param messageId 消息唯一ID
     * @return true 表示已经有消费者处理过或正在处理，false 表示本次成功抢占处理权
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis)
     */
    public boolean isMessageBeingConsumed(String messageId) {
        String key = buildKey(messageId);
        Boolean firstConsume = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, PROCESSING_STATUS, PROCESSING_TTL);
        return Boolean.FALSE.equals(firstConsume);
    }

    /**
     * 判断消息消费流程是否已经完成。
     *
     * @param messageId 消息唯一ID
     * @return 是否已经完成
     * 本方法使用的工具类: Objects(JDK),StringRedisTemplate(spring-data-redis)
     */
    public boolean isAccomplish(String messageId) {
        return Objects.equals(stringRedisTemplate.opsForValue().get(buildKey(messageId)), ACCOMPLISH_STATUS);
    }

    /**
     * 设置消息消费流程已完成。
     *
     * @param messageId 消息唯一ID
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis)
     */
    public void setAccomplish(String messageId) {
        stringRedisTemplate.opsForValue().set(buildKey(messageId), ACCOMPLISH_STATUS, ACCOMPLISH_TTL);
    }

    /**
     * 删除消息处理标记，允许 RabbitMQ 后续重试重新消费。
     *
     * @param messageId 消息唯一ID
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis)
     */
    public void delMessageProcessed(String messageId) {
        stringRedisTemplate.delete(buildKey(messageId));
    }

    /**
     * 构建消息幂等 Redis Key。
     *
     * @param messageId 消息唯一ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    private String buildKey(String messageId) {
        return IDEMPOTENT_KEY_PREFIX + messageId;
    }
}

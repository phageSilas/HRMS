package com.hrms.common.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 公共配置，统一使用 Jackson JSON 序列化消息体。
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 配置 RabbitMQ JSON 消息转换器。
     *
     * @param objectMapper Spring Boot 全局 Jackson 对象映射器
     * @return RabbitMQ 消息转换器
     * 本方法使用的工具类: Jackson2JsonMessageConverter(spring-amqp),ObjectMapper(jackson)
     */
    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

package com.hrms.common.mq;

/**
 * HRMS RabbitMQ 消息基础接口，所有业务消息必须携带全局唯一消息ID。
 */
public interface HrmsMqMessage {

    /**
     * 获取消息唯一ID。
     *
     * @return 消息唯一ID
     * 本方法使用的工具类: 无
     */
    String getMessageId();
}

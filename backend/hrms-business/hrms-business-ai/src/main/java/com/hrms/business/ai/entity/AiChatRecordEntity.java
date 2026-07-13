package com.hrms.business.ai.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * AI对话记录实体
 */
@Data
public class AiChatRecordEntity extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 问题
     */
    private String question;

    /**
     * 回答
     */
    private String answer;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 消耗token数
     */
    private Integer tokenCount;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 状态：1-成功，0-失败
     */
    private Integer status;

}

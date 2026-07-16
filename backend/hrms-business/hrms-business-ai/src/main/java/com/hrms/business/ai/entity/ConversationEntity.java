package com.hrms.business.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 对话记录实体
 *
 * @TableName hr_ai_conversation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_ai_conversation")
public class ConversationEntity extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对话标题（取首条消息前 20 字）
     */
    private String title;

    /**
     * 状态：1-活跃 2-已归档
     */
    private Integer status;

    /**
     * 消息总数
     */
    private Integer messageCount;

}

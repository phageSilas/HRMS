package com.hrms.business.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 消息记录实体
 *
 * @TableName hr_ai_message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_ai_message")
public class MessageEntity extends BaseEntity {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 元数据（JSON 格式，存储意图、引用来源等）
     */
    private String metadata;

}

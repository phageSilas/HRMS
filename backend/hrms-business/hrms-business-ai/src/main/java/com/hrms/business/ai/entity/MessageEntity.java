package com.hrms.business.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 消息记录实体
 * <p>
 * 映射表 hr_ai_message，存储用户与 AI 助手的每一条消息。
 * 用户消息（role=user）和 AI 回复（role=assistant）均存储在此表。
 * 元数据字段存储路由建议、引用来源等额外信息。
 * </p>
 *
 * @since 2026-07-20
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

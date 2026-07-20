package com.hrms.business.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.ai.entity.MessageEntity;

/**
 * 消息 Mapper
 * <p>
 * 提供对 {@link MessageEntity}（hr_ai_message）的持久化操作。
 * 负责 AI 对话中用户消息和助手消息的增删改查，由 MyBatis-Plus 自动提供实现。
 * </p>
 *
 * @since 2026-07-20
 */
public interface AiMessageMapper extends BaseMapper<MessageEntity> {

}

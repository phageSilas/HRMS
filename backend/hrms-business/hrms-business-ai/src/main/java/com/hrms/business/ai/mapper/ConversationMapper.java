package com.hrms.business.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.ai.entity.ConversationEntity;

/**
 * 会话 Mapper
 * <p>
 * 提供对 {@link ConversationEntity}（hr_ai_conversation）的持久化操作。
 * 负责 AI 对话会话的增删改查，由 MyBatis-Plus 自动提供实现。
 * </p>
 *
 * @since 2026-07-20
 */
public interface ConversationMapper extends BaseMapper<ConversationEntity> {

}

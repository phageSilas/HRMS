package com.hrms.business.ai.service;

import com.hrms.business.ai.dto.ChatRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话服务接口
 * <p>
 * 负责处理用户消息、调用 DashScope 百炼流式生成、集成百炼知识库检索。
 *
 * @since 2026-07-20
 */
public interface AiChatService {

    /**
     * 发送消息并获取 SSE 流式响应
     *
     * @param userId  用户ID
     * @param request 聊天请求（含会话ID、消息内容）
     * @return SseEmitter SSE 发射器，用于流式推送
     */
    SseEmitter chatStream(Long userId, ChatRequestDTO request);

}

package com.hrms.business.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.business.ai.config.AiConfig;
import com.hrms.business.ai.dto.ChatRequestDTO;
import com.hrms.business.ai.entity.ConversationEntity;
import com.hrms.business.ai.entity.MessageEntity;
import com.hrms.business.ai.enums.AiErrorCode;
import com.hrms.business.ai.mapper.ConversationMapper;
import com.hrms.business.ai.mapper.AiMessageMapper;
import com.hrms.business.ai.service.AiChatService;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * AI 对话服务实现
 * <p>
 * 核心流程：保存用户消息 → 构建 Prompt → 调 DashScope 百炼（流式）→ 保存 AI 回复 → SSE 返回
 * <p>
 * DashScope（阿里云百炼）统一提供：
 * - LLM 对话生成（通义千问）
 * - 知识库 RAG 检索（百炼知识库，需配置 knowledgeBaseId）
 * <p>
 * 知识库未配置时不影响基础对话，AI 仅凭自身知识回答。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiConfig aiConfig;
    private final ConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public SseEmitter chatStream(Long userId, ChatRequestDTO request) {
        // ========== 1. 创建/获取会话 ==========
        Long conversationId = request.getConversationId();
        if (conversationId == null) {
            conversationId = createConversation(userId, request.getContent());
        } else {
            checkConversationOwnership(userId, conversationId);
        }

        // ========== 2. 保存用户消息 ==========
        saveUserMessage(conversationId, request.getContent());
        incrementMessageCount(conversationId);

        // ========== 3. 创建 SseEmitter（无超时） ==========
        SseEmitter emitter = new SseEmitter(0L);

        // ========== 4. 发送 start 事件 ==========
        try {
            String startData = objectMapper.writeValueAsString(
                    Map.of("type", "start", "conversationId", conversationId));
            emitter.send(SseEmitter.event().name("message").data(startData));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        // ========== 5. 异步调用 DashScope 百炼 ==========
        Long finalConversationId = conversationId;
        CompletableFuture.runAsync(() -> {
            try {
                // 查询历史消息
                List<MessageEntity> history = messageMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<MessageEntity>lambdaQuery()
                                .eq(MessageEntity::getConversationId, finalConversationId)
                                .orderByAsc(MessageEntity::getCreateTime));

                // DashScope 知识库检索（如未配置则返回空）
                String knowledgeContext = retrieveKnowledge(request.getContent());

                // 调用 DashScope 百炼流式生成
                String fullResponse = callDashScope(emitter, history, request.getContent(), knowledgeContext);

                // 发送 end 事件
                String endData = objectMapper.writeValueAsString(
                        Map.of("type", "end", "reason", "stop"));
                emitter.send(SseEmitter.event().name("message").data(endData));
                emitter.complete();

                // 保存 AI 回复
                saveAiResponse(finalConversationId, fullResponse);

            } catch (Exception e) {
                log.error("AI 对话流式处理异常", e);
                try {
                    String errorData = objectMapper.writeValueAsString(
                            Map.of("type", "error", "code", 40121, "message", "AI 服务调用失败，请稍后重试"));
                    emitter.send(SseEmitter.event().name("message").data(errorData));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // ==================== DashScope 百炼调用 ====================

    /**
     * 调用 DashScope 百炼 API（OpenAI 兼容模式）流式生成回答
     *
     * @return 完整 AI 回答文本
     */
    private String callDashScope(SseEmitter emitter, List<MessageEntity> history,
                                 String userContent, String knowledgeContext) throws IOException {
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GlobalException(AiErrorCode.AI_SERVICE_ERROR);
        }

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(knowledgeContext)));

        // 历史消息（最近 20 条 ≈ 10 轮）
        int startIdx = Math.max(0, history.size() - 20);
        for (int i = startIdx; i < history.size(); i++) {
            MessageEntity msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // 当前用户问题
        messages.add(Map.of("role", "user", "content", userContent));

        // 构建请求 JSON（OpenAI 兼容格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // OkHttp 客户端
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .readTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .build();

        Request request = new Request.Builder()
                .url(aiConfig.getBaseUrl() + "/v1/chat/completions")
                .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        StringBuilder fullContent = new StringBuilder();

        // 使用锁同步等待流式完成
        Object lock = new Object();

        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if ("[DONE]".equals(data)) {
                    return;
                }
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                    if (choices == null || choices.isEmpty()) return;

                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                    if (delta == null) return;

                    String content = (String) delta.get("content");
                    if (content != null && !content.isEmpty()) {
                        fullContent.append(content);
                        String contentEvent = objectMapper.writeValueAsString(
                                Map.of("type", "content", "text", content));
                        try {
                            emitter.send(SseEmitter.event().name("message").data(contentEvent));
                        } catch (IOException e) {
                            eventSource.cancel();
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.warn("解析 DashScope SSE 数据失败: {}", data, e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                log.error("DashScope SSE 连接失败", t);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        // 等待流式完成或失败
        try {
            synchronized (lock) {
                lock.wait(aiConfig.getTimeout());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return fullContent.toString();
    }

    // ==================== Prompt 构建 ====================

    private String buildSystemPrompt(String knowledgeContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 HRMS（人力资源管理系统）的智能助手。请根据以下规则回答：\n\n");
        sb.append("1. 如果你擅长的问题，请直接回答。\n");
        sb.append("2. 如果用户询问公司制度或政策，请优先使用提供的知识片段回答。\n");
        sb.append("3. 如果知识片段不足以回答，请说明不确定，不要编造信息。\n");
        sb.append("4. 回答应简洁、专业、友好，使用中文。\n");

        if (knowledgeContext != null && !knowledgeContext.isBlank()) {
            sb.append("\n以下是相关制度文档片段供参考：\n");
            sb.append(knowledgeContext);
        }

        return sb.toString();
    }

    // ==================== 百炼知识库检索 ====================

    /**
     * DashScope 百炼知识库检索
     * <p>
     * 用户提问时检索公司制度相关文档片段，拼入 Prompt 上下文。
     * 未配置 knowledgeBaseId 时返回空，不影响基础对话。
     * <p>
     * TODO: 接入 DashScope Knowledge Base Retrieve API
     * API 地址：POST https://dashscope.aliyuncs.com/api/v2/apps/memory/search
     * 参考：https://help.aliyun.com/zh/model-studio/memory-library
     */
    private String retrieveKnowledge(String query) {
        String apiKey = aiConfig.getApiKey();
        String kbId = aiConfig.getKnowledgeBaseId();
        if (apiKey != null && !apiKey.isBlank() && kbId != null && !kbId.isBlank()) {
            log.info("DashScope 知识库已配置（ID: {}），检索集成待后续实现。配置后请求时将自动检索相关知识片段拼入 Prompt", kbId);
            // TODO: 调用 DashScope SearchMemory API
        }
        return "";
    }

    // ==================== 数据库操作 ====================

    private Long createConversation(Long userId, String content) {
        String title = content;
        if (title != null && title.length() > 20) {
            title = title.substring(0, 20);
        }
        ConversationEntity entity = new ConversationEntity();
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setStatus(1);
        entity.setMessageCount(0);
        conversationMapper.insert(entity);
        return entity.getId();
    }

    private void checkConversationOwnership(Long userId, Long conversationId) {
        ConversationEntity conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new GlobalException(AiErrorCode.CONVERSATION_NOT_FOUND);
        }
    }

    private void saveUserMessage(Long conversationId, String content) {
        MessageEntity msg = new MessageEntity();
        msg.setConversationId(conversationId);
        msg.setRole("user");
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    public void saveAiResponse(Long conversationId, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        MessageEntity msg = new MessageEntity();
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(content);
        messageMapper.insert(msg);
        incrementMessageCount(conversationId);
    }

    private void incrementMessageCount(Long conversationId) {
        ConversationEntity entity = conversationMapper.selectById(conversationId);
        if (entity != null) {
            entity.setMessageCount(
                    Optional.ofNullable(entity.getMessageCount()).orElse(0) + 1);
            conversationMapper.updateById(entity);
        }
    }

}

package com.hrms.business.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 对话服务实现
 * <p>
 * 核心流程：保存用户消息 → 调百炼 App Completion API（流式）→ 保存 AI 回复 → SSE 返回
 * <p>
 * DashScope（阿里云百炼）统一提供：
 * - LLM 对话生成（通义千问）
 * - 知识库 RAG 检索（百炼应用知识库，需在控制台创建应用并关联知识库）
 * <p>
 * 通过百炼 App Completion API，一个请求完成「检索知识库 → 构建 Prompt → LLM 生成」全过程。
 * 未配置应用 ID 时自动回退到基础 LLM 对话。
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

        final Long cId = conversationId;

        // ========== 3. 创建 SseEmitter（超时后自动结束） ==========
        SseEmitter emitter = new SseEmitter(aiConfig.getTimeout());

        // 注册完成/超时/错误回调（确保最终清理）
        emitter.onCompletion(() -> log.debug("SSE 连接完成, conversationId={}", cId));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时, conversationId={}", cId);
            emitter.complete();
        });
        emitter.onError(t -> log.error("SSE 连接异常, conversationId={}", cId, t));

        // ========== 4. 发送 start 事件 ==========
        try {
            String startData = objectMapper.writeValueAsString(
                    Map.of("type", "start", "conversationId", conversationId));
            emitter.send(SseEmitter.event().name("message").data(startData));
        } catch (IOException e) {
            log.error("发送 start 事件失败", e);
            return emitter;
        }

        // ========== 5. 异步调用百炼 API ==========
        Long finalConversationId = conversationId;
        CompletableFuture.runAsync(() -> {
            try {
                // 查询历史消息
                List<MessageEntity> history = messageMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<MessageEntity>lambdaQuery()
                                .eq(MessageEntity::getConversationId, finalConversationId)
                                .orderByAsc(MessageEntity::getCreateTime));

                // 调用百炼应用 App Completion API（应用自动处理知识库检索 + LLM 生成）
                String fullResponse = callDashScopeApp(emitter, history, request.getContent());

                // 解析路由建议（从 AI 回复中提取）
                List<Map<String, String>> suggestions = parseSuggestions(fullResponse);

                // 发送 end 事件（含路由建议）
                Map<String, Object> endPayload = new HashMap<>();
                endPayload.put("type", "end");
                endPayload.put("reason", "stop");
                if (!suggestions.isEmpty()) {
                    endPayload.put("suggestions", suggestions);
                }
                String endData = objectMapper.writeValueAsString(endPayload);
                emitter.send(SseEmitter.event().name("message").data(endData));
                emitter.complete();

                // 保存 AI 回复（含路由建议）
                saveAiResponse(finalConversationId, fullResponse, suggestions);

            } catch (Exception e) {
                log.error("AI 对话流式处理异常", e);
                try {
                    String errorData = objectMapper.writeValueAsString(
                            Map.of("type", "error", "code", 40121, "message", e.getMessage() != null ? e.getMessage() : "AI 服务调用失败，请稍后重试"));
                    emitter.send(SseEmitter.event().name("message").data(errorData));
                } catch (IOException ex) {
                    log.warn("发送 error 事件失败", ex);
                }
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        return emitter;
    }

    // ==================== 百炼 App Completion API 调用 ====================

    /**
     * 调用百炼 App Completion API（流式）
     * <p>
     * 应用已在百炼控制台配置了系统 Prompt 和知识库，
     * 一个 API 请求完成：知识库检索 → Prompt 构建 → LLM 生成。
     * <p>
     * 未配置 appId 时自动回退到直接 LLM 对话（无知识库）。
     *
     * @return 完整 AI 回答文本
     */
    private String callDashScopeApp(SseEmitter emitter, List<MessageEntity> history,
                                     String userContent) throws IOException {
        String apiKey = aiConfig.getApiKey();
        String appId = aiConfig.getAppId();

        // 校验 API Key
        if (apiKey == null || apiKey.isBlank()) {
            throw new GlobalException(AiErrorCode.AI_SERVICE_ERROR);
        }

        // 未配置 AppId 时回退到基础 LLM 对话
        if (appId == null || appId.isBlank()) {
            return callDirectLLM(emitter, history, userContent);
        }

        // ===== 构建对话历史字符串 =====
        StringBuilder promptBuilder = new StringBuilder();
        int startIdx = Math.max(0, history.size() - 20);
        for (int i = startIdx; i < history.size(); i++) {
            MessageEntity msg = history.get(i);
            String roleLabel = "user".equals(msg.getRole()) ? "用户" : "助手";
            promptBuilder.append(roleLabel).append("：").append(msg.getContent()).append("\n");
        }
        promptBuilder.append("用户：").append(userContent);

        // ===== 构建 App API 请求体 =====
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", promptBuilder.toString());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("incremental_output", true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // ===== OkHttp 客户端 =====
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .readTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .build();

        Request request = new Request.Builder()
                .url(aiConfig.getBaseUrl() + "/api/v1/apps/" + appId + "/completion")
                .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-SSE", "enable")
                .build();

        StringBuilder fullContent = new StringBuilder();
        boolean[] markerReached = {false};
        Object lock = new Object();
        AtomicReference<String> apiError = new AtomicReference<>(null);

        // ===== SSE 流式接收 =====
        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                // App API 的事件类型为 "result"
                if (!"result".equals(type)) return;

                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> eventData = objectMapper.readValue(data, Map.class);
                    Map<String, Object> output = (Map<String, Object>) eventData.get("output");
                    if (output == null) return;

                    String text = (String) output.get("text");
                    if (text == null || text.isEmpty()) return;

                    fullContent.append(text);

                    // 已经到达 SUGGESTIONS 标记，不再推送
                    if (markerReached[0]) return;

                    // 检测当前完整内容中是否包含 SUGGESTIONS 标记
                    String currentFull = fullContent.toString();
                    int markerIdx = currentFull.indexOf("---SUGGESTIONS---");
                    if (markerIdx >= 0) {
                        markerReached[0] = true;
                        // 只推送标记前的内容
                        int prevLen = fullContent.length() - text.length();
                        if (markerIdx > prevLen) {
                            String safePart = text.substring(0, markerIdx - prevLen);
                            if (!safePart.isEmpty()) {
                                try {
                                    emitter.send(SseEmitter.event().name("message")
                                            .data(objectMapper.writeValueAsString(
                                                    Map.of("type", "content", "text", safePart))));
                                } catch (IOException e) {
                                    eventSource.cancel();
                                }
                            }
                        }
                        return;
                    }

                    // 检查标记被跨 chunk 分割的情况
                    String pushText = text;
                    int maxOverlap = Math.min("---SUGGESTIONS---".length(), currentFull.length());
                    for (int i = 1; i <= maxOverlap; i++) {
                        String suffix = currentFull.substring(currentFull.length() - i);
                        if ("---SUGGESTIONS---".startsWith(suffix)) {
                            pushText = text.substring(0, text.length() - Math.min(i, text.length()));
                            break;
                        }
                    }

                    // 转发到前端 SSE
                    if (!pushText.isEmpty()) {
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data(objectMapper.writeValueAsString(
                                            Map.of("type", "content", "text", pushText))));
                        } catch (IOException e) {
                            eventSource.cancel();
                        }
                    }

                } catch (JsonProcessingException e) {
                    log.warn("解析 App API SSE 数据失败: {}", data, e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                if (response != null) {
                    try {
                        String respBody = response.body() != null ? response.body().string() : "null";
                        log.error("App API SSE 连接失败: HTTP {}, body={}", response.code(), respBody);
                        apiError.set("HTTP " + response.code() + ": " + respBody);
                    } catch (IOException ignored) {
                        apiError.set(t.getMessage());
                    }
                } else {
                    apiError.set(t.getMessage());
                }
                log.error("App API SSE 连接失败, 异常详情: ", t);
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

        // 如果 API 调用失败，抛出异常让上层 catch 块向前端发送错误事件
        String errMsg = apiError.get();
        if (errMsg != null) {
            throw new IOException("百炼应用 API 调用失败: " + errMsg);
        }

        return fullContent.toString();
    }

    /**
     * 回退：直接调用 DashScope LLM（OpenAI 兼容模式，无知识库）
     * 当未配置百炼应用 appId 时使用。
     */
    private String callDirectLLM(SseEmitter emitter, List<MessageEntity> history,
                                  String userContent) throws IOException {
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GlobalException(AiErrorCode.AI_SERVICE_ERROR);
        }

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "你是 HRMS（人力资源管理系统）的智能助手。请用中文简洁、专业地回答用户问题。"));

        // 历史消息（最近 20 条 ≈ 10 轮）
        int startIdx = Math.max(0, history.size() - 20);
        for (int i = startIdx; i < history.size(); i++) {
            MessageEntity msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        messages.add(Map.of("role", "user", "content", userContent));

        // 构建请求 JSON（OpenAI 兼容格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .readTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                .build();

        Request request = new Request.Builder()
                .url(aiConfig.getBaseUrl() + "/compatible-mode/v1/chat/completions")
                .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        StringBuilder fullContent = new StringBuilder();
        Object lock = new Object();

        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if ("[DONE]".equals(data)) return;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                    if (choices == null || choices.isEmpty()) return;

                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                    if (delta == null) return;

                    String chunkText = (String) delta.get("content");
                    if (chunkText == null || chunkText.isEmpty()) return;

                    fullContent.append(chunkText);

                    try {
                        emitter.send(SseEmitter.event().name("message")
                                .data(objectMapper.writeValueAsString(
                                        Map.of("type", "content", "text", chunkText))));
                    } catch (IOException e) {
                        eventSource.cancel();
                    }
                } catch (JsonProcessingException e) {
                    log.warn("解析 LLM SSE 数据失败: {}", data, e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                if (response != null) {
                    try {
                        String respBody = response.body() != null ? response.body().string() : "null";
                        log.error("LLM SSE 连接失败: HTTP {}, body={}", response.code(), respBody);
                    } catch (IOException ignored) {
                    }
                }
                log.error("LLM SSE 连接失败, 异常详情: ", t);
                synchronized (lock) { lock.notifyAll(); }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                synchronized (lock) { lock.notifyAll(); }
            }
        });

        try {
            synchronized (lock) {
                lock.wait(aiConfig.getTimeout());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (fullContent.length() == 0) {
            log.warn("LLM 返回空内容，可能是 API 调用失败");
        }

        return fullContent.toString();
    }

    // ==================== 路由建议解析 ====================

    /**
     * 从 AI 完整回复中解析路由建议
     * <p>
     * AI 被要求以 ---SUGGESTIONS--- 标记输出路由建议 JSON。
     */
    private List<Map<String, String>> parseSuggestions(String fullResponse) {
        if (fullResponse == null || !fullResponse.contains("---SUGGESTIONS---")) {
            return List.of();
        }

        int markerIdx = fullResponse.indexOf("---SUGGESTIONS---");
        int jsonStart = markerIdx + "---SUGGESTIONS---".length();

        while (jsonStart < fullResponse.length()
                && Character.isWhitespace(fullResponse.charAt(jsonStart))) {
            jsonStart++;
        }

        try {
            String jsonPart = fullResponse.substring(jsonStart).trim();
            int jsonEnd = jsonPart.indexOf('\n');
            if (jsonEnd > 0) {
                jsonPart = jsonPart.substring(0, jsonEnd);
            }

            List<Map<String, String>> suggestions = objectMapper.readValue(
                    jsonPart, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {});
            return suggestions != null ? suggestions : List.of();
        } catch (Exception e) {
            log.warn("解析路由建议失败", e);
            return List.of();
        }
    }

    /**
     * 从 AI 完整回复中移除 SUGGESTIONS 标记和 JSON，返回纯文本
     */
    private String stripSuggestions(String fullResponse) {
        if (fullResponse == null) return "";
        int markerIdx = fullResponse.indexOf("---SUGGESTIONS---");
        if (markerIdx < 0) return fullResponse;
        return fullResponse.substring(0, markerIdx).trim();
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

    /**
     * 保存 AI 回复（自动去除 SUGGESTIONS 标记，将路由建议存入 metadata）
     */
    public void saveAiResponse(Long conversationId, String fullResponse,
                               List<Map<String, String>> suggestions) {
        if (fullResponse == null || fullResponse.isBlank()) {
            return;
        }

        // 去除 SUGGESTIONS 标记，只保存纯文本
        String cleanContent = stripSuggestions(fullResponse);
        if (cleanContent.isBlank()) {
            return;
        }

        MessageEntity msg = new MessageEntity();
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(cleanContent);

        // 如果有路由建议，存入 metadata
        if (!suggestions.isEmpty()) {
            try {
                Map<String, Object> meta = new HashMap<>();
                meta.put("suggestions", suggestions);
                msg.setMetadata(objectMapper.writeValueAsString(meta));
            } catch (JsonProcessingException e) {
                log.warn("序列化路由建议失败", e);
            }
        }

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

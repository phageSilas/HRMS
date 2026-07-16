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
        boolean[] markerReached = {false}; // 使用数组以便在 lambda 中修改

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

                    String chunkText = (String) delta.get("content");
                    if (chunkText == null || chunkText.isEmpty()) return;

                    fullContent.append(chunkText);

                    // 已经到达 SUGGESTIONS 标记，不再推送
                    if (markerReached[0]) return;

                    String currentFull = fullContent.toString();
                    int markerIdx = currentFull.indexOf("---SUGGESTIONS---");
                    if (markerIdx >= 0) {
                        markerReached[0] = true;
                        // 标记在此 chunk 中完成，只推送标记前的内容
                        int prevLen = fullContent.length() - chunkText.length();
                        if (markerIdx > prevLen) {
                            String safePart = chunkText.substring(0, markerIdx - prevLen);
                            if (!safePart.isEmpty()) {
                                try {
                                    emitter.send(SseEmitter.event().name("message")
                                            .data(objectMapper.writeValueAsString(Map.of("type", "content", "text", safePart))));
                                } catch (IOException e) {
                                    eventSource.cancel();
                                }
                            }
                        }
                        return;
                    }

                    // 检查当前 fullContent 末尾是否匹配 marker 的前缀（标记被跨 chunk 分割时）
                    // 如以 "---" "---SUG" 结尾时，不推送重叠部分
                    String pushText = chunkText;
                    int maxOverlap = Math.min("---SUGGESTIONS---".length(), currentFull.length());
                    for (int i = 1; i <= maxOverlap; i++) {
                        String suffix = currentFull.substring(currentFull.length() - i);
                        if ("---SUGGESTIONS---".startsWith(suffix)) {
                            pushText = chunkText.substring(0, chunkText.length() - Math.min(i, chunkText.length()));
                            break;
                        }
                    }

                    if (!pushText.isEmpty()) {
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data(objectMapper.writeValueAsString(Map.of("type", "content", "text", pushText))));
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
        sb.append("你是 HRMS（人力资源管理系统）的智能助手。请严格遵守以下规则：\n\n");

        sb.append("## 回答规则\n");
        sb.append("1. 如果用户询问公司制度或政策，请优先使用提供的知识片段回答，并标注引用来源。\n");
        sb.append("2. 如果知识片段不足以回答，请说明不确定，不要编造信息。\n");
        sb.append("3. 回答应简洁、专业、友好，使用中文。\n");

        sb.append("\n## 意图识别与路由建议\n");
        sb.append("识别用户的操作意图，在回答末尾输出路由建议。格式如下：\n\n");
        sb.append("---SUGGESTIONS---\n");
        sb.append("[{\"label\":\"去请假\",\"path\":\"/attendance/leave\"}]\n\n");
        sb.append("### 可识别的意图与对应路由\n");
        sb.append("- 请假相关 → /attendance/leave\n");
        sb.append("- 查工资/薪资/工资条 → /salary/payslip\n");
        sb.append("- 查考勤记录 → /attendance/record\n");
        sb.append("- 查加班 → /attendance/record\n");
        sb.append("- 调岗/转岗 → /process/transfer\n");
        sb.append("- 入职申请 → /process/entry\n");
        sb.append("- 转正申请 → /process/regular\n");
        sb.append("- 离职申请 → /process/leave\n");
        sb.append("- 查员工信息 → /employee/list\n");
        sb.append("- 查个人档案 → /profile/index\n");
        sb.append("- 审批/待审批 → /approval/workspace\n\n");
        sb.append("如果无法识别明确意图，不输出 SUGGESTIONS 块。\n");
        sb.append("如果识别到多个意图，可以输出多条建议。\n");

        if (knowledgeContext != null && !knowledgeContext.isBlank()) {
            sb.append("\n## 知识库参考\n");
            sb.append("以下是相关制度文档片段供参考：\n");
            sb.append(knowledgeContext);
        }

        return sb.toString();
    }

    // ==================== 百炼知识库检索 ====================

    /**
     * DashScope 百炼知识库检索
     * <p>
     * 调用 DashScope SearchMemory API 检索知识库，返回相关文档片段。
     * 未配置 knowledgeBaseId 时返回空字符串。
     */
    @SuppressWarnings("unchecked")
    private String retrieveKnowledge(String query) {
        String apiKey = aiConfig.getApiKey();
        String kbId = aiConfig.getKnowledgeBaseId();
        if (apiKey == null || apiKey.isBlank() || kbId == null || kbId.isBlank()) {
            return "";
        }

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .callTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                    .readTimeout(java.time.Duration.ofMillis(aiConfig.getTimeout()))
                    .build();

            Map<String, Object> body = new HashMap<>();
            body.put("memory_id", kbId);
            body.put("query", query);
            body.put("top_k", 5);
            body.put("threshold", 0.5);

            Request request = new Request.Builder()
                    .url("https://dashscope.aliyuncs.com/api/v2/apps/memory/search")
                    .post(RequestBody.create(objectMapper.writeValueAsString(body),
                            okhttp3.MediaType.parse("application/json")))
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("DashScope 知识库检索失败: HTTP {}", response.code());
                    return "";
                }

                String respBody = response.body().string();
                Map<String, Object> result = objectMapper.readValue(respBody, Map.class);

                // 解析返回的 chunks
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data == null) return "";

                List<Map<String, Object>> chunks = (List<Map<String, Object>>) data.get("chunks");
                if (chunks == null || chunks.isEmpty()) return "";

                StringBuilder context = new StringBuilder();
                for (int i = 0; i < chunks.size(); i++) {
                    Map<String, Object> chunk = chunks.get(i);
                    String content = (String) chunk.get("content");
                    String source = (String) chunk.get("source");
                    if (content != null && !content.isBlank()) {
                        context.append("\n[").append(i + 1).append("] ");
                        if (source != null) {
                            context.append("（来源：").append(source).append("）");
                        }
                        context.append(content).append("\n");
                    }
                }

                if (context.length() > 0) {
                    log.info("知识库检索成功，获取到 {} 个相关片段", chunks.size());
                }
                return context.toString();
            }
        } catch (Exception e) {
            log.error("DashScope 知识库检索异常", e);
            return "";
        }
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

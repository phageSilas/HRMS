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
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Qualifier;

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
public class AiChatServiceImpl implements AiChatService {

    private final AiConfig aiConfig;
    private final ConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final ObjectMapper objectMapper;
    private final Executor aiChatExecutor;

    public AiChatServiceImpl(AiConfig aiConfig, ConversationMapper conversationMapper,
                             AiMessageMapper messageMapper, ObjectMapper objectMapper,
                             @Qualifier("aiChatExecutor") Executor aiChatExecutor) {
        this.aiConfig = aiConfig;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
        this.aiChatExecutor = aiChatExecutor;
    }

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
        final boolean isNewConversation = request.getConversationId() == null;
        final String firstUserMessage = request.getContent();

        // ========== 3. 创建 SseEmitter（超时后自动结束） ==========
        SseEmitter emitter = new SseEmitter(aiConfig.getTimeout());

        // 注册完成/超时/错误回调（确保最终清理）
        emitter.onCompletion(() -> log.debug("SSE 连接完成, conversationId={}", cId));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时, conversationId={}", cId);
            // 注意：不要在此处调用 emitter.complete()，Spring 在超时后
            // 会自动完成 emitter，重复调用会抛出 IllegalStateException
        });
        emitter.onError(t -> log.error("SSE 连接异常, conversationId={}", cId, t));

        // ========== 4. 异步调用百炼 API ==========
        // 使用专用线程池处理 AI 对话流，避免 ForkJoinPool.commonPool() 的守护线程问题
        //
        // 注意：start 事件的发送也在异步任务中执行，确保：
        // 1. emitter 先返回给 Spring MVC，Spring 正确设置 Content-Type: text/event-stream
        // 2. Spring 进入异步模式（request.startAsync()）
        // 3. 异步任务再发送数据，避免在同步模式下提前提交 response
        Long finalConversationId = conversationId;
        CompletableFuture.runAsync(() -> {
            String fullResponse = null;
            List<Map<String, String>> suggestions = List.of();
            String aiTitle = null;

            try {
                // 发送 start 事件（确保在异步线程中执行，此时 Spring MVC 已完成异步模式初始化）
                // 同步线程中 emitter.send() 会提前提交 response，导致后续异步写入失败。
                String startData = objectMapper.writeValueAsString(
                        Map.of("type", "start", "conversationId", finalConversationId));
                emitter.send(SseEmitter.event().name("message").data(startData));

                // 查询历史消息
                List<MessageEntity> history = messageMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<MessageEntity>lambdaQuery()
                                .eq(MessageEntity::getConversationId, finalConversationId)
                                .orderByAsc(MessageEntity::getCreateTime));

                // 调用百炼应用 App Completion API（应用自动处理知识库检索 + LLM 生成）
                // 如果是新会话，会自动在回复末尾添加 ---TITLE--- 标记输出标题
                fullResponse = callDashScopeApp(emitter, history, request.getContent(), isNewConversation);
                log.info("[AI_RESPONSE] 完整响应长度={}, 最后200字符=[{}]",
                        fullResponse != null ? fullResponse.length() : 0,
                        fullResponse != null && fullResponse.length() > 200 ? fullResponse.substring(fullResponse.length() - 200) : fullResponse);

                // 解析路由建议（从 AI 回复中提取）
                suggestions = parseSuggestions(fullResponse);

                // 如果是新会话，解析 AI 生成的标题
                if (isNewConversation) {
                    aiTitle = parseTitle(fullResponse);
                    log.info("[AI_TITLE] 解析标题: {}", aiTitle);
                }

                // 再发送 end 事件（含路由建议）
                Map<String, Object> endPayload = new HashMap<>();
                endPayload.put("type", "end");
                endPayload.put("reason", "stop");
                if (!suggestions.isEmpty()) {
                    endPayload.put("suggestions", suggestions);
                }
                String endData = objectMapper.writeValueAsString(endPayload);
                emitter.send(SseEmitter.event().name("message").data(endData));
                emitter.complete();

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
            } finally {
                // 无论正常完成还是异常，都保存已收到的 AI 回复到数据库
                if (fullResponse != null && !fullResponse.isBlank()) {
                    saveAiResponse(finalConversationId, fullResponse, suggestions);

                    // 更新会话标题（异常路径下也尝试解析）
                    if (aiTitle == null && isNewConversation) {
                        aiTitle = parseTitle(fullResponse);
                    }
                    if (aiTitle != null && !aiTitle.isBlank()) {
                        ConversationEntity entity = conversationMapper.selectById(finalConversationId);
                        if (entity != null) {
                            entity.setTitle(aiTitle);
                            conversationMapper.updateById(entity);
                            log.info("[AI_TITLE] 更新会话标题: {} → {}",
                                    firstUserMessage.length() > 20 ? firstUserMessage.substring(0, 20) + "..." : firstUserMessage,
                                    aiTitle);
                        }
                    }
                }
            }
        }, aiChatExecutor);

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
                                     String userContent, boolean isNewConversation) throws IOException {
        String apiKey = aiConfig.getApiKey();
        String appId = aiConfig.getAppId();

        // 校验 API Key
        if (apiKey == null || apiKey.isBlank()) {
            throw new GlobalException(AiErrorCode.AI_SERVICE_ERROR);
        }

        // 未配置 AppId 时回退到基础 LLM 对话
        if (appId == null || appId.isBlank()) {
            return callDirectLLM(emitter, history, userContent, isNewConversation);
        }

        // ===== 构建对话历史字符串 =====
        StringBuilder promptBuilder = new StringBuilder();
        int startIdx = Math.max(0, history.size() - 20);
        for (int i = startIdx; i < history.size(); i++) {
            MessageEntity msg = history.get(i);
            String roleLabel = "user".equals(msg.getRole()) ? "用户" : "助手";
            promptBuilder.append(roleLabel).append("：").append(msg.getContent()).append("\n");
        }
        // 新会话：在用户消息后添加标题生成指令
        String promptContent = userContent;
        if (isNewConversation) {
            promptContent = userContent + "\n\n（在回答结束时，用 ---TITLE--- 标记输出一个不超过10个字的对话标题，概括用户意图。直接输出标题文字，不要引号）";
        }
        // 路由建议指令：指导 AI 在适当时输出功能页面快捷链接
        promptContent += "\n\n如果你认为需要推荐 HRMS 相关功能页面的快捷链接，请在回答最后用 ---SUGGESTIONS--- 标记输出 JSON 数组。" +
                "格式：[{\"label\":\"页面名称\",\"path\":\"/路由路径\"}]。" +
                "可用的路由路径有：/employee/list（员工列表）、/attendance/punch（员工打卡）、/attendance/record（考勤记录）、" +
                "/attendance/leaveManage（请假管理）、/attendance/summary（考勤统计）、" +
                "/process/entry（入职申请）、/process/regular（转正申请）、/process/transfer（调岗申请）、/process/leave（离职申请）、" +
                "/salary/account（薪资账套）、/salary/payslip（工资条）、/salary/batch（薪资核算）、" +
                "/approval/workspace（审批工作台）、/approval/delegation（委托审批）、" +
                "/organization/dept（部门管理）、/organization/post（职位管理）、" +
                "/system/user（用户管理）、/system/role（角色管理）、" +
                "/profile/index（个人首页）、/profile/attendance（我的考勤）、/profile/salary（我的薪资）。" +
                "只推荐与用户问题相关的页面，最多推荐3个。不要推荐无关页面。";
        promptBuilder.append("用户：").append(promptContent);

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

        // ===== 使用 BlockingQueue 解耦 OkHttp 回调与 SseEmitter 写入 =====
        // OkHttp EventSource 回调线程将原始内容块放入队列，
        // 当前线程（async 线程）从队列中取出，统一处理标记检测和 SseEmitter 转发，
        // 避免跨线程写入 Servlet 输出流带来的潜在缓冲问题
        BlockingQueue<String> chunkQueue = new LinkedBlockingQueue<>();
        AtomicBoolean streamFinished = new AtomicBoolean(false);
        AtomicReference<String> apiError = new AtomicReference<>(null);

        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {

            /**
             * 收到百炼 SSE 事件回调（OkHttp 回调线程）
             * <p>
             * 仅做两件事：解析响应体提取 text，放入队列。不做标记检测和 emitter.send()。
             * </p>
             */
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

                    // 只放入队列，由主 polling 线程处理转发
                    chunkQueue.offer(text);

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
                streamFinished.set(true);
            }

            @Override
            public void onClosed(EventSource eventSource) {
                streamFinished.set(true);
            }
        });

        // ===== 主线程轮询：从队列读取内容块，标记检测，SseEmitter 转发 =====
        StringBuilder fullContent = new StringBuilder();
        long deadline = System.currentTimeMillis() + aiConfig.getTimeout();
        boolean markerReached = false;
        int lastForwardedLength = 0;           // fullContent 中已转发到前端的字符长度
        String[] markers = {"---SUGGESTIONS---", "---TITLE---", "--TITLE---"};

        while (System.currentTimeMillis() < deadline && !streamFinished.get()) {
            String chunk;
            try {
                // 每次轮询最多等待 200ms，之后检查 streamFinished 状态
                chunk = chunkQueue.poll(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (chunk == null) {
                continue;           // 队列超时，非结束信号，继续轮询
            }

            fullContent.append(chunk);
            log.debug("[SSE_CHUNK] text=[{}]", chunk);

            // 已检测到标记 → 只积累不转发
            if (markerReached) {
                log.debug("[SSE_MARKER] 已到达标记，跳过推送 text=[{}]", chunk);
                continue;
            }

            String currentFull = fullContent.toString();

            // 检测当前完整内容中是否包含 SUGGESTIONS 或 TITLE 标记
            String foundMarker = null;
            int earliestMarker = -1;
            for (String m : markers) {
                int idx = currentFull.indexOf(m);
                if (idx >= 0 && (earliestMarker < 0 || idx < earliestMarker)) {
                    earliestMarker = idx;
                    foundMarker = m;
                }
            }

            if (foundMarker != null) {
                // 标记出现，只转发标记前的内容
                markerReached = true;
                log.debug("[SSE_MARKER] 找到标记 [{}] 在位置 {}, currentFull尾=[{}]",
                        foundMarker, earliestMarker,
                        currentFull.length() > 200 ? currentFull.substring(Math.max(0, currentFull.length() - 200)) : currentFull);

                // 计算本次 chunk 中需要转发到前端的部分（标记前的内容）
                int chunkStartInFull = currentFull.length() - chunk.length();
                if (earliestMarker > chunkStartInFull) {
                    // 标记在当前 chunk 中，截取 chunk 内标记前的内容
                    String forwardPart = chunk.substring(0, earliestMarker - chunkStartInFull);
                    if (!forwardPart.isEmpty()) {
                        log.debug("[SSE_MARKER] 转发标记前内容 [{}]",
                                forwardPart.length() > 100 ? forwardPart.substring(0, 100) + "..." : forwardPart);
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data(objectMapper.writeValueAsString(
                                            Map.of("type", "content", "text", forwardPart))));
                        } catch (IOException e) {
                            log.warn("SseEmitter 发送失败，客户端可能已断开", e);
                            break;
                        }
                    }
                } else {
                    log.debug("[SSE_MARKER] 标记在之前的 chunk 中就已存在，当前 chunk 全部跳过");
                }
                lastForwardedLength = currentFull.length();
            } else {
                // 无标记，检查当前累计内容尾部是否与标记前缀重叠（跨 chunk 标记）
                int trimLen = 0;
                for (String m : markers) {
                    int limit = Math.min(m.length(), currentFull.length());
                    for (int i = 1; i <= limit; i++) {
                        String suffix = currentFull.substring(currentFull.length() - i);
                        if (m.startsWith(suffix)) {
                            trimLen = Math.max(trimLen, i);
                        }
                    }
                }

                // 提取"尚未转发"的内容（从 lastForwardedLength 到末尾）
                String newContent = currentFull.substring(lastForwardedLength);

                if (trimLen > 0) {
                    // 尾部与标记前缀重叠 → 去掉重叠部分再转发
                    String forwardText = newContent.substring(0, Math.max(0, newContent.length() - trimLen));
                    log.debug("[SSE_CROSS] 尾部与标记重叠 {} 字符, chunk=[{}], forwardText=[{}]",
                            trimLen, chunk, forwardText);
                    if (!forwardText.isEmpty()) {
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data(objectMapper.writeValueAsString(
                                            Map.of("type", "content", "text", forwardText))));
                        } catch (IOException e) {
                            log.warn("SseEmitter 发送失败，客户端可能已断开", e);
                            break;
                        }
                    }
                } else {
                    // 正常转发新增的完整内容
                    if (!newContent.isEmpty()) {
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data(objectMapper.writeValueAsString(
                                            Map.of("type", "content", "text", newContent))));
                        } catch (IOException e) {
                            log.warn("SseEmitter 发送失败，客户端可能已断开", e);
                            break;
                        }
                    }
                }
                lastForwardedLength = currentFull.length();
            }
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
                                  String userContent, boolean isNewConversation) throws IOException {
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GlobalException(AiErrorCode.AI_SERVICE_ERROR);
        }

        // 构建消息列表
        String routeSuggestionPrompt = "如果你认为需要推荐 HRMS 相关功能页面的快捷链接，请在回答最后用 ---SUGGESTIONS--- 标记输出 JSON 数组。" +
                "格式：[{\"label\":\"页面名称\",\"path\":\"/路由路径\"}]。" +
                "可用的路由路径有：/employee/list（员工列表）、/attendance/punch（员工打卡）、/attendance/record（考勤记录）、" +
                "/attendance/leaveManage（请假管理）、/attendance/summary（考勤统计）、" +
                "/process/entry（入职申请）、/process/regular（转正申请）、/process/transfer（调岗申请）、/process/leave（离职申请）、" +
                "/salary/account（薪资账套）、/salary/payslip（工资条）、/salary/batch（薪资核算）、" +
                "/approval/workspace（审批工作台）、/approval/delegation（委托审批）、" +
                "/organization/dept（部门管理）、/organization/post（职位管理）、" +
                "/system/user（用户管理）、/system/role（角色管理）、" +
                "/profile/index（个人首页）、/profile/attendance（我的考勤）、/profile/salary（我的薪资）。" +
                "只推荐与用户问题相关的页面，最多推荐3个。不要推荐无关页面。";
        String sysPrompt = "你是 HRMS（人力资源管理系统）的智能助手。请用中文简洁、专业地回答用户问题。\n\n" + routeSuggestionPrompt;
        if (isNewConversation) {
            sysPrompt += "\n\n在回答结束时，用 ---TITLE--- 标记输出一个不超过10个字的对话标题，概括用户意图。直接输出标题文字，不要引号。";
        }
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", sysPrompt));

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
        CountDownLatch latch = new CountDownLatch(1);

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
                latch.countDown();
            }

            @Override
            public void onClosed(EventSource eventSource) {
                latch.countDown();
            }
        });

        try {
            // 使用 CountDownLatch.await 替代 Object.wait，避免 notify 在 wait 之前发出导致的信号丢失
            boolean completed = latch.await(aiConfig.getTimeout(), TimeUnit.MILLISECONDS);
            if (!completed) {
                log.warn("LLM 流式响应超时 ({}ms)，已收到 {} 字符", aiConfig.getTimeout(), fullContent.length());
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
            // 从标记后提取完整 JSON 数组（支持多行格式）
            String jsonPart = fullResponse.substring(jsonStart).trim();

            // 查找匹配的 JSON 数组结束位置（处理多行 JSON）
            int bracketDepth = 0;
            int jsonEnd = -1;
            for (int i = 0; i < jsonPart.length(); i++) {
                char c = jsonPart.charAt(i);
                if (c == '[') {
                    bracketDepth++;
                } else if (c == ']') {
                    bracketDepth--;
                    if (bracketDepth == 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
            }
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
     * 从 AI 完整回复中移除 SUGGESTIONS 和 TITLE 标记及后续内容，返回纯文本
     */
    private String stripSuggestions(String fullResponse) {
        if (fullResponse == null) return "";
        String text = fullResponse;
        // 先移除 ---TITLE--- 或 --TITLE--- 标记及之后内容
        int titleIdx = text.indexOf("---TITLE---");
        if (titleIdx < 0) titleIdx = text.indexOf("--TITLE---");
        if (titleIdx >= 0) text = text.substring(0, titleIdx).trim();
        // 再移除 ---SUGGESTIONS--- 标记及之后内容
        int suggIdx = text.indexOf("---SUGGESTIONS---");
        if (suggIdx >= 0) text = text.substring(0, suggIdx).trim();
        return text;
    }

    /**
     * 从 AI 完整回复中解析 ---TITLE---（或 --TITLE---）标记后的标题
     */
    private String parseTitle(String fullResponse) {
        if (fullResponse == null) {
            log.debug("[TITLE_PARSE] fullResponse 为 null");
            return null;
        }
        // 兼容 3 短横线和 2 短横线变体
        int markerIdx = fullResponse.indexOf("---TITLE---");
        if (markerIdx < 0) {
            markerIdx = fullResponse.indexOf("--TITLE---");
        }
        if (markerIdx < 0) {
            log.debug("[TITLE_PARSE] 未找到任何 TITLE 标记, lastChars=[{}]",
                    fullResponse.length() > 30 ? fullResponse.substring(fullResponse.length() - 30) : fullResponse);
            return null;
        }

        // 确定实际使用的标记长度
        String after;
        if (fullResponse.substring(markerIdx).startsWith("---TITLE---")) {
            after = fullResponse.substring(markerIdx + "---TITLE---".length()).trim();
        } else {
            after = fullResponse.substring(markerIdx + "--TITLE---".length()).trim();
        }
        // 取第一行（标题在一行内），去掉末尾标点
        int endIdx = after.indexOf('\n');
        if (endIdx > 0) after = after.substring(0, endIdx).trim();
        // 去掉末尾的句号等标点
        after = after.replaceAll("[。，！？.!?,;；]+$", "").trim();
        if (after.length() > 20) after = after.substring(0, 20);
        log.debug("[TITLE_PARSE] 从位置 {} 解析标题: [{}]", markerIdx, after);
        return after.isBlank() ? null : after;
    }

    // ==================== 数据库操作 ====================

    /**
     * 创建新会话
     * <p>
     * 使用用户的第一条消息作为默认标题（超过 20 字时截断）。
     * AI 生成回复后将通过 parseTitle 解析出更精确的标题并更新。
     * </p>
     *
     * @param userId  用户 ID
     * @param content 用户的第一条消息内容
     * @return 新建会话 ID
     */
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

    /**
     * 校验会话归属权
     *
     * @param userId         用户 ID
     * @param conversationId 会话 ID
     * @throws GlobalException 会话不存在或不属于该用户时抛出
     */
    private void checkConversationOwnership(Long userId, Long conversationId) {
        ConversationEntity conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new GlobalException(AiErrorCode.CONVERSATION_NOT_FOUND);
        }
    }

    /**
     * 保存用户消息到数据库
     *
     * @param conversationId 会话 ID
     * @param content        用户消息内容
     */
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
            log.warn("[SAVE_RESP] fullResponse 为空或空白，跳过保存, conversationId={}", conversationId);
            return;
        }

        // 去除 SUGGESTIONS 标记，只保存纯文本
        String cleanContent = stripSuggestions(fullResponse);
        if (cleanContent.isBlank()) {
            log.warn("[SAVE_RESP] stripSuggestions 后内容为空, conversationId={}, fullResponse=[{}]",
                    conversationId,
                    fullResponse.length() > 100 ? fullResponse.substring(0, 100) + "..." : fullResponse);
            return;
        }

        MessageEntity msg = new MessageEntity();
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(cleanContent);

        log.info("[SAVE_RESP] 保存 AI 回复, conversationId={}, 内容长度={}, 前50字=[{}]",
                conversationId, cleanContent.length(),
                cleanContent.length() > 50 ? cleanContent.substring(0, 50) : cleanContent);

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

    /**
     * 递增会话的消息计数
     *
     * @param conversationId 会话 ID
     */
    private void incrementMessageCount(Long conversationId) {
        ConversationEntity entity = conversationMapper.selectById(conversationId);
        if (entity != null) {
            entity.setMessageCount(
                    Optional.ofNullable(entity.getMessageCount()).orElse(0) + 1);
            conversationMapper.updateById(entity);
        }
    }

}

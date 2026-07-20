package com.hrms.business.ai.controller;

import com.hrms.business.ai.dto.ChatRequestDTO;
import com.hrms.business.ai.dto.UpdateTitleRequestDTO;
import com.hrms.business.ai.service.AiChatService;
import com.hrms.business.ai.service.ConversationService;
import com.hrms.business.ai.vo.ConversationDetailVO;
import com.hrms.business.ai.vo.ConversationVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能助手控制器
 * <p>
 * 提供 AI 对话的 SSE 流式聊天接口和会话管理接口。
 * API-AI-01 发送消息（SSE 流式）
 * API-AI-02 会话列表（分页）
 * API-AI-03 消息历史
 * API-AI-04 删除会话
 * API-AI-05 修改标题
 *
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI 智能助手", description = "AI 对话、会话管理接口")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;
    private final ConversationService conversationService;

    // ==================== API-AI-01：发送消息 ====================

    /**
     * 发送消息（SSE 流式返回）
     * <p>
     * 接收用户消息，调用 DashScope 百炼流式生成回答，通过 SSE 逐步返回。
     * 同时会查询百炼知识库获取相关文档片段作为上下文。
     * </p>
     *
     * @param request 聊天请求（含会话ID和消息内容）
     * @return SseEmitter SSE 流式发射器，前端通过 EventSource 接收
     */
    @PostMapping("/chat")
    @Operation(summary = "发送消息", description = "发送消息并 SSE 流式获取 AI 回答")
    public SseEmitter chat(@Valid @RequestBody ChatRequestDTO request) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        return aiChatService.chatStream(userId, request);
    }

    // ==================== API-AI-02：会话列表 ====================

    /**
     * 获取当前用户的会话列表
     * <p>分页查询，按最后更新时间倒序排列。</p>
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页条数（默认 20）
     * @return 分页会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "会话列表", description = "分页查询当前用户的 AI 对话列表")
    public Result<PageResult<ConversationVO>> listConversations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityContextHolder.getUserId();
        PageResult<ConversationVO> result = conversationService.listConversations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    // ==================== API-AI-03：消息记录 ====================

    /**
     * 获取指定会话的消息列表
     * <p>返回会话的完整消息历史，按发送时间升序排列。</p>
     *
     * @param id 会话ID
     * @return 会话详情（含消息列表）
     */
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "消息记录", description = "获取指定会话的完整消息历史")
    public Result<ConversationDetailVO> getMessages(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getUserId();
        ConversationDetailVO detail = conversationService.getConversationDetail(userId, id);
        return Result.success(detail);
    }

    // ==================== API-AI-04：删除会话 ====================

    /**
     * 删除会话
     * <p>使用 MyBatis-Plus 逻辑删除，数据不物理移除。</p>
     *
     * @param id 会话ID
     */
    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除会话", description = "逻辑删除指定会话")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getUserId();
        conversationService.deleteConversation(userId, id);
        return Result.success();
    }

    // ==================== API-AI-05：修改标题 ====================

    /**
     * 修改会话标题
     *
     * @param id      会话ID
     * @param request 标题更新请求（含新标题）
     */
    @PutMapping("/conversations/{id}/title")
    @Operation(summary = "修改标题", description = "修改指定会话的标题")
    public Result<Void> updateTitle(@PathVariable Long id,
                                    @Valid @RequestBody UpdateTitleRequestDTO request) {
        Long userId = SecurityContextHolder.getUserId();
        conversationService.updateTitle(userId, id, request.getTitle());
        return Result.success();
    }

}

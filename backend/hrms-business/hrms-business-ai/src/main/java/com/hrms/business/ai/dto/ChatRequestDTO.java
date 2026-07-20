package com.hrms.business.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求 DTO
 * <p>
 * 封装用户发送给 AI 助手的消息内容。
 * 如果 {@code conversationId} 为空，服务端会自动创建新会话；
 * 否则消息将追加到指定会话中。
 * </p>
 *
 * @since 2026-07-20
 */
@Data
public class ChatRequestDTO {

    /**
     * 会话ID
     * <p>为空时表示创建新会话，不为空时追加到已有会话</p>
     */
    private Long conversationId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

}

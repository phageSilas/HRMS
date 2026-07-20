package com.hrms.business.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求 DTO
 */
@Data
public class ChatRequestDTO {

    /**
     * 会话ID（null 表示创建新会话）
     */
    private Long conversationId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

}

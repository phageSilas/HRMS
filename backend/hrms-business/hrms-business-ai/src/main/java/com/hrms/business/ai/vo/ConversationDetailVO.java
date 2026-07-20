package com.hrms.business.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 会话详情 VO
 * <p>
 * 包含指定 AI 会话的标题和完整消息列表。
 * 用于会话详情页展示，按消息创建时间升序排列。
 * </p>
 *
 * @since 2026-07-20
 */
@Data
public class ConversationDetailVO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 消息列表
     * <p>按发送时间升序排列</p>
     */
    private List<MessageVO> messages;

}

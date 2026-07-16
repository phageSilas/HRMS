package com.hrms.business.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 会话详情 VO（含消息列表）
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
     */
    private List<MessageVO> messages;

}

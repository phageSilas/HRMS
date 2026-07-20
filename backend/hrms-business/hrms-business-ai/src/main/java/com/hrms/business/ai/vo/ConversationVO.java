package com.hrms.business.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表 VO
 */
@Data
public class ConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 最后一条消息摘要
     */
    private String lastMessage;

    /**
     * 状态：1-活跃 2-已归档
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

}

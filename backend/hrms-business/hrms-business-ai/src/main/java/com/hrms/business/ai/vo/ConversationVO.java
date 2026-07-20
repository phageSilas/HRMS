package com.hrms.business.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表 VO
 * <p>
 * 展示 AI 对话会话的摘要信息，用于会话列表页。
 * 每条会话附带最后一条消息的摘要（前 50 字），方便用户快速识别会话内容。
 * </p>
 *
 * @since 2026-07-20
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
     * <p>用户消息 + AI 回复的总条数</p>
     */
    private Integer messageCount;

    /**
     * 最后一条消息摘要
     * <p>超过 50 字时截断并追加"..."</p>
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

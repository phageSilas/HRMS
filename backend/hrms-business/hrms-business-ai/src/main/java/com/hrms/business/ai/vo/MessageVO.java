package com.hrms.business.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息 VO
 * <p>
 * 表示 AI 对话中的一条消息记录。
 * 用于会话详情页展示完整的对话上下文。
 * </p>
 *
 * @since 2026-07-20
 */
@Data
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 角色
     * <p>user-用户消息 / assistant-AI助手回复</p>
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 元数据（JSON 格式）
     * <p>可包含路由建议（suggestions）、引用来源（citations）、置信度（confidence）等</p>
     */
    private String metadata;

    /**
     * 发送时间
     */
    private LocalDateTime createTime;

}

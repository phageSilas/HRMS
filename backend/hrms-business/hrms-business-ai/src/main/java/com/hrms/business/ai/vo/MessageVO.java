package com.hrms.business.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息 VO
 */
@Data
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 元数据（JSON，含 citations/confidence 等）
     */
    private String metadata;

    /**
     * 发送时间
     */
    private LocalDateTime createTime;

}

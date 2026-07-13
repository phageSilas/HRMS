package com.hrms.business.mycenter.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 个人消息实体
 */
@Data
public class MessageEntity extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-系统通知，2-审批提醒，3-薪资通知
     */
    private Integer type;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 关联业务类型
     */
    private String bizType;

}

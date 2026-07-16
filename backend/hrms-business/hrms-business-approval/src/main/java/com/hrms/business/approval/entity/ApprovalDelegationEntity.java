package com.hrms.business.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 委托审批实体
 * <p>
 * 对应数据库表 hr_approval_delegation
 * </p>
 */
@Data
@TableName("hr_approval_delegation")
public class ApprovalDelegationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 委托人用户ID
     */
    private Long delegatorId;

    /**
     * 委托人姓名
     */
    private String delegatorName;

    /**
     * 被委托人用户ID
     */
    private Long delegateToId;

    /**
     * 被委托人姓名
     */
    private String delegateToName;

    /**
     * 委托生效时间
     */
    private LocalDateTime startDate;

    /**
     * 委托结束时间
     */
    private LocalDateTime endDate;

    /**
     * 委托原因
     */
    private String reason;

    /**
     * 状态：0-已取消 1-生效中 2-已过期
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

package com.hrms.business.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批任务实体
 * <p>
 * 对应数据库表 hr_approval_task
 * </p>
 */
@Data
@TableName("hr_approval_task")
public class ApprovalTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 节点编码
     */
    private String nodeCode;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 实际审批人用户ID
     */
    private Long approverUserId;

    /**
     * 原审批人（委托场景）
     */
    private Long originalApproverId;

    /**
     * 是否代审：0-本人 1-代审
     */
    private Integer delegateFlag;

    /**
     * 任务状态：0-待处理 1-已处理 2-已转交
     */
    private Integer taskStatus;

    /**
     * 结果：1-通过 2-驳回 3-转交
     */
    private Integer approveResult;

    /**
     * 审批意见
     */
    private String approveComment;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 截止时间（超时升级依据）
     */
    private LocalDateTime deadlineTime;

    /**
     * 节点顺序
     */
    private Integer sortNo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

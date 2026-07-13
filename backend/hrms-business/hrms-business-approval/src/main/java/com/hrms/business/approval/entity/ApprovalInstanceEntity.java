package com.hrms.business.approval.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 审批实例实体
 */
@Data
public class ApprovalInstanceEntity extends BaseEntity {

    /**
     * 审批类型
     */
    private String approvalType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 当前节点
     */
    private Integer currentNode;

    /**
     * 总节点数
     */
    private Integer totalNode;

    /**
     * 状态：0-草稿，1-审批中，2-已通过，3-已驳回，4-已撤回
     */
    private Integer status;

    /**
     * 审批意见
     */
    private String comment;

}

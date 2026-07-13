package com.hrms.business.approval.service;

/**
 * 审批中心服务接口
 */
public interface ApprovalService {

    /**
     * 发起审批
     *
     * @param type   审批类型
     * @param bizId  业务ID
     * @return 审批实例ID
     */
    Long startApproval(String type, Long bizId);

    /**
     * 审批通过
     *
     * @param taskId     任务ID
     * @param comment    审批意见
     */
    void approve(Long taskId, String comment);

    /**
     * 审批驳回
     *
     * @param taskId     任务ID
     * @param comment    审批意见
     */
    void reject(Long taskId, String comment);

    /**
     * 委托审批
     *
     * @param taskId       任务ID
     * @param delegateToId 被委托人ID
     */
    void delegate(Long taskId, Long delegateToId);

}

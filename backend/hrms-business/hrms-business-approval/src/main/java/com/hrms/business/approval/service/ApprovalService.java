package com.hrms.business.approval.service;

/**
 * 审批中心服务接口
 * <p>
 * 供其他业务模块（入转调离、考勤、薪资）调用的审批服务入口。
 * </p>
 */
public interface ApprovalService {

    /**
     * 发起审批（简化版，不含表单快照）
     *
     * @param type  审批类型编码
     * @param bizId 业务主键ID
     * @return 审批实例ID
     */
    Long startApproval(String type, Long bizId);

    /**
     * 发起审批（含表单快照）
     *
     * @param type     审批类型编码
     * @param bizId    业务主键ID
     * @param formData 表单快照（JSON 字符串）
     * @return 审批实例ID
     */
    Long startApproval(String type, Long bizId, String formData);

    /**
     * 审批通过
     *
     * @param taskId  任务ID
     * @param comment 审批意见
     */
    void approve(Long taskId, String comment);

    /**
     * 审批驳回
     *
     * @param taskId  任务ID
     * @param comment 审批意见
     */
    void reject(Long taskId, String comment);

    /**
     * 转交审批
     *
     * @param taskId       任务ID
     * @param delegateToId 被转交人用户ID
     */
    void delegate(Long taskId, Long delegateToId);

}

package com.hrms.business.approval.service;

/**
 * 审批人解析器
 * <p>
 * 根据审批人类型和业务上下文解析具体的审批人用户ID，
 * 并在委托场景下自动替换为被委托人。
 * </p>
 */
public interface ApproverResolver {

    /**
     * 解析审批人
     *
     * @param approverType    审批人类型：DEPT_HEAD / SUPERIOR_DEPT_HEAD / HR_HEAD / FINANCE_HEAD / BOSS
     * @param applicantDeptId 申请人部门ID（用于解析部门负责人等）
     * @param bizId           业务ID（调岗时需要区分原/新部门）
     * @return 审批人用户ID，无法解析时返回 null
     */
    Long resolveApprover(String approverType, Long applicantDeptId, Long bizId);

    /**
     * 检查委托关系
     * <p>
     * 查询是否有生效中的委托，如果有则返回被委托人ID。
     * 该方法由审批引擎在创建任务时调用，实现自动委托替换。
     * </p>
     *
     * @param approverUserId 原审批人用户ID
     * @return 被委托人用户ID，无委托时返回 null
     */
    Long checkDelegation(Long approverUserId);
}

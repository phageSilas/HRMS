package com.hrms.business.approval.service;

import com.hrms.business.approval.dto.OperateResultVO;

/**
 * 审批流程引擎
 * <p>
 * 审批中心的核心引擎，统一处理审批流程的发起和操作流转。
 * 系分文档 §3.7.7 定义的核心组件。
 * </p>
 */
public interface ApprovalEngine {

    /**
     * 发起审批
     *
     * @param approvalType       审批类型编码
     * @param bizId              业务主键ID
     * @param formData           表单快照（JSON 字符串）
     * @param applicantUserId    申请人用户ID
     * @param applicantDeptId    申请人部门ID（用于解析审批人）
     * @param applicantEmployeeId 申请人员工ID（可为 null）
     * @return 审批实例ID
     */
    Long startApproval(String approvalType, Long bizId, String formData,
                       Long applicantUserId, Long applicantDeptId, Long applicantEmployeeId);

    /**
     * 处理审批操作
     *
     * @param taskId       审批任务ID
     * @param action       操作类型：approve（通过）/ reject（驳回）/ transfer（转交）
     * @param comment      审批意见
     * @param targetUserId 转交目标用户ID（action=transfer 时必填）
     * @return 操作结果（含任务状态、实例状态、下一节点名称）
     * @throws com.hrms.common.exception.GlobalException 任务已处理时抛出 CONFLICT
     */
    OperateResultVO processAction(Long taskId, String action, String comment, Long targetUserId);
}

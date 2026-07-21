package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalService;
import com.hrms.common.security.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审批中心服务实现
 * <p>
 * 作为对外的统一入口，委托 {@link ApprovalEngine} 完成核心流转。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalEngine approvalEngine;

    /**
     * 发起审批（无表单数据）
     *
     * @param type  审批业务类型
     * @param bizId 业务主键 ID
     * @return 审批实例 ID
     */
    @Override
    public Long startApproval(String type, Long bizId) {
        return startApproval(type, bizId, null);
    }

    /**
     * 发起审批（带表单数据）
     * <p>
     * 从安全上下文中获取当前用户 ID 和部门 ID 作为申请人信息。
     * </p>
     *
     * @param type     审批业务类型
     * @param bizId    业务主键 ID
     * @param formData 表单数据 JSON 快照
     * @return 审批实例 ID
     */
    @Override
    public Long startApproval(String type, Long bizId, String formData) {
        Long userId = SecurityContextHolder.getUserId();
        Long deptId = SecurityContextHolder.getDeptId();
        return approvalEngine.startApproval(type, bizId, formData, userId, deptId, null);
    }

    /**
     * 审批通过
     *
     * @param taskId  审批任务 ID
     * @param comment 审批意见
     */
    @Override
    public void approve(Long taskId, String comment) {
        approvalEngine.processAction(taskId, "approve", comment, null);
    }

    /**
     * 审批驳回
     *
     * @param taskId  审批任务 ID
     * @param comment 驳回原因
     */
    @Override
    public void reject(Long taskId, String comment) {
        approvalEngine.processAction(taskId, "reject", comment, null);
    }

    /**
     * 转交审批任务
     *
     * @param taskId       审批任务 ID
     * @param delegateToId 转交目标用户 ID
     */
    @Override
    public void delegate(Long taskId, Long delegateToId) {
        approvalEngine.processAction(taskId, "transfer", null, delegateToId);
    }

}

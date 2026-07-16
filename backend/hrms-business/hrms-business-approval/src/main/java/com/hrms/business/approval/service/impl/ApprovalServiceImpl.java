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

    @Override
    public Long startApproval(String type, Long bizId) {
        return startApproval(type, bizId, null);
    }

    @Override
    public Long startApproval(String type, Long bizId, String formData) {
        Long userId = SecurityContextHolder.getUserId();
        Long deptId = SecurityContextHolder.getDeptId();
        return approvalEngine.startApproval(type, bizId, formData, userId, deptId, null);
    }

    @Override
    public void approve(Long taskId, String comment) {
        approvalEngine.processAction(taskId, "approve", comment, null);
    }

    @Override
    public void reject(Long taskId, String comment) {
        approvalEngine.processAction(taskId, "reject", comment, null);
    }

    @Override
    public void delegate(Long taskId, Long delegateToId) {
        approvalEngine.processAction(taskId, "transfer", null, delegateToId);
    }

}

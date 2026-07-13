package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.service.ApprovalService;
import org.springframework.stereotype.Service;

/**
 * 审批中心服务实现
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Override
    public Long startApproval(String type, Long bizId) {
        // TODO: 实现发起审批逻辑
        return null;
    }

    @Override
    public void approve(Long taskId, String comment) {
        // TODO: 实现审批通过逻辑
    }

    @Override
    public void reject(Long taskId, String comment) {
        // TODO: 实现审批驳回逻辑
    }

    @Override
    public void delegate(Long taskId, Long delegateToId) {
        // TODO: 实现委托审批逻辑
    }

}

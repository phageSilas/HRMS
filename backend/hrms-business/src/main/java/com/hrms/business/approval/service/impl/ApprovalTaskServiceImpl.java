package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.dto.ApprovalHandleRequestDTO;
import com.hrms.business.approval.dto.ApprovalStartRequestDTO;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.approval.vo.ApprovalTaskVO;
import org.springframework.stereotype.Service;

/**
 * 实现审批中心业务接口能力。
 */
@Service
public class ApprovalTaskServiceImpl implements ApprovalTaskService {

    /**
     * 发起审批任务。
     *
     * @param requestParam 审批发起请求参数
     * @return 审批任务信息
     */
    @Override
    public ApprovalTaskVO start(ApprovalStartRequestDTO requestParam) {
        return new ApprovalTaskVO(1L, requestParam.bizType(), requestParam.bizId(), "PROCESSING");
    }

    /**
     * 查询审批任务详情。
     *
     * @param taskId 审批任务ID
     * @return 审批任务信息
     */
    @Override
    public ApprovalTaskVO getById(Long taskId) {
        return new ApprovalTaskVO(taskId, "EMPLOYEE_PROCESS", 1L, "PROCESSING");
    }

    /**
     * 通过审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     */
    @Override
    public void approve(Long taskId, ApprovalHandleRequestDTO requestParam) {
    }

    /**
     * 驳回审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     */
    @Override
    public void reject(Long taskId, ApprovalHandleRequestDTO requestParam) {
    }
}

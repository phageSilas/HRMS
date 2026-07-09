package com.hrms.business.approval.service;

import com.hrms.business.approval.dto.ApprovalHandleRequestDTO;
import com.hrms.business.approval.dto.ApprovalStartRequestDTO;
import com.hrms.business.approval.vo.ApprovalTaskVO;

/**
 * 定义审批中心业务接口能力。
 */
public interface ApprovalTaskService {

    /**
     * 发起审批任务。
     *
     * @param requestParam 审批发起请求参数
     * @return 审批任务信息
     */
    ApprovalTaskVO start(ApprovalStartRequestDTO requestParam);

    /**
     * 查询审批任务详情。
     *
     * @param taskId 审批任务ID
     * @return 审批任务信息
     */
    ApprovalTaskVO getById(Long taskId);

    /**
     * 通过审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     */
    void approve(Long taskId, ApprovalHandleRequestDTO requestParam);

    /**
     * 驳回审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     */
    void reject(Long taskId, ApprovalHandleRequestDTO requestParam);
}

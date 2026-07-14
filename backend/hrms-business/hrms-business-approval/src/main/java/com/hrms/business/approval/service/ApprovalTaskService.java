package com.hrms.business.approval.service;

import com.hrms.business.approval.dto.ApprovalDetailVO;
import com.hrms.business.approval.dto.PendingTaskQuery;
import com.hrms.business.approval.dto.PendingTaskVO;
import com.hrms.common.web.PageResult;

/**
 * 审批任务服务
 * <p>
 * 审批工作台的查询与操作服务，调用 {@link ApprovalEngine} 执行核心流转。
 * </p>
 */
public interface ApprovalTaskService {

    /**
     * 待办列表
     *
     * @param userId 当前用户ID
     * @param query  查询参数
     * @return 分页待办任务列表
     */
    PageResult<PendingTaskVO> findPendingTasks(Long userId, PendingTaskQuery query);

    /**
     * 已审批列表
     *
     * @param userId 当前用户ID
     * @param query  查询参数
     * @return 分页已审批任务列表
     */
    PageResult<PendingTaskVO> findHistoryTasks(Long userId, PendingTaskQuery query);

    /**
     * 我发起的申请
     *
     * @param userId 当前用户ID
     * @param query  查询参数
     * @return 分页申请列表
     */
    PageResult<PendingTaskVO> findMyApplications(Long userId, PendingTaskQuery query);

    /**
     * 审批详情
     *
     * @param instanceId    审批实例ID
     * @param currentUserId 当前用户ID
     * @return 审批详情
     */
    ApprovalDetailVO getDetail(Long instanceId, Long currentUserId);

    /**
     * 撤回申请
     *
     * @param instanceId 审批实例ID
     * @param userId     当前用户ID（必须是申请人）
     */
    void withdraw(Long instanceId, Long userId);

    /**
     * 获取待审批数量
     *
     * @param userId 用户ID
     * @return 待审批任务数
     */
    Integer getPendingCount(Long userId);
}

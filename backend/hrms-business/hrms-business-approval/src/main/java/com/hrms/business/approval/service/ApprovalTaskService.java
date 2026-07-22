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

    /**
     * 按筛选类型查询任务列表（支持待审批/今日已审批/已逾期）
     *
     * @param userId 当前用户ID
     * @param query  查询参数（含 filterType）
     * @return 分页任务列表
     */
    PageResult<PendingTaskVO> findFilteredTasks(Long userId, PendingTaskQuery query);

    /**
     * 获取今日已审批数量
     *
     * @param userId 用户ID
     * @return 今日已审批任务数
     */
    Integer getTodayApprovedCount(Long userId);

    /**
     * 获取已逾期数量
     *
     * @param userId 用户ID
     * @return 逾期的待办任务数
     */
    Integer getOverdueCount(Long userId);

    /**
     * 按审批实例ID查询当前待办任务ID。
     *
     * @param instanceId 审批实例ID
     * @return 当前待办任务ID，不存在时返回 null
     */
    Long getCurrentPendingTaskIdByInstanceId(Long instanceId);
}

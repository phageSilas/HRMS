package com.hrms.business.approval.service;

import com.hrms.business.approval.dto.DelegationCreateRequest;
import com.hrms.business.approval.dto.DelegationListVO;

/**
 * 委托审批服务
 */
public interface DelegationService {

    /**
     * 创建委托
     *
     * @param userId  委托人用户ID
     * @param request 委托请求
     * @return 委托ID
     */
    Long createDelegation(Long userId, DelegationCreateRequest request);

    /**
     * 取消委托
     *
     * @param id     委托ID
     * @param userId 当前用户ID（必须是委托人）
     */
    void cancelDelegation(Long id, Long userId);

    /**
     * 查询我的委托
     *
     * @param userId 当前用户ID
     * @return 委托列表（含生效中 + 历史记录）
     */
    DelegationListVO findMyDelegations(Long userId);
}

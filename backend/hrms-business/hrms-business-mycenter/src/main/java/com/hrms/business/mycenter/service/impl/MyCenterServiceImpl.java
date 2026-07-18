package com.hrms.business.mycenter.service.impl;

import com.hrms.business.approval.dto.PendingTaskQuery;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.mycenter.dto.ProfileUpdateRequest;
import com.hrms.business.mycenter.dto.ProfileVO;
import com.hrms.business.mycenter.service.MyCenterService;
import com.hrms.business.mycenter.service.ProfileService;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 个人中心服务实现
 * <p>
 * 聚合个人档案、审批模块的数据，提供个人中心首页一站式数据。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyCenterServiceImpl implements MyCenterService {

    private final ProfileService profileService;
    private final ApprovalTaskService approvalTaskService;

    @Override
    public ProfileVO getProfile(Long userId) {
        return profileService.getProfile(userId);
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest profile) {
        profileService.updateProfile(userId, profile);
    }

    @Override
    public PageResult<?> getMyApplications(Long userId) {
        PendingTaskQuery query = new PendingTaskQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        return approvalTaskService.findMyApplications(userId, query);
    }

    @Override
    public PageResult<?> getMyApprovals(Long userId) {
        PendingTaskQuery query = new PendingTaskQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        return approvalTaskService.findPendingTasks(userId, query);
    }

}

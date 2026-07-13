package com.hrms.business.mycenter.service.impl;

import com.hrms.business.mycenter.service.MyCenterService;
import org.springframework.stereotype.Service;

/**
 * 个人中心服务实现
 */
@Service
public class MyCenterServiceImpl implements MyCenterService {

    @Override
    public Object getProfile(Long userId) {
        // TODO: 实现获取个人信息逻辑
        return null;
    }

    @Override
    public void updateProfile(Long userId, Object profile) {
        // TODO: 实现更新个人信息逻辑
    }

    @Override
    public Object getMyApplications(Long userId) {
        // TODO: 实现获取我的申请列表逻辑
        return null;
    }

    @Override
    public Object getMyApprovals(Long userId) {
        // TODO: 实现获取我的审批列表逻辑
        return null;
    }

}

package com.hrms.business.mycenter.service;

import com.hrms.business.mycenter.dto.ProfileUpdateRequest;
import com.hrms.business.mycenter.dto.ProfileVO;

/**
 * 个人档案服务接口
 */
public interface ProfileService {

    /**
     * 获取当前用户的个人档案
     */
    ProfileVO getProfile(Long userId);

    /**
     * 更新个人档案
     */
    void updateProfile(Long userId, ProfileUpdateRequest request);
}

package com.hrms.business.mycenter.service;

import com.hrms.business.mycenter.dto.ProfileUpdateRequest;
import com.hrms.business.mycenter.dto.ProfileVO;
import com.hrms.common.web.PageResult;

/**
 * 个人中心服务接口
 */
public interface MyCenterService {

    /**
     * 获取个人档案
     *
     * @param userId 用户ID
     * @return 个人档案
     */
    ProfileVO getProfile(Long userId);

    /**
     * 更新个人档案
     *
     * @param userId  用户ID
     * @param profile 更新请求
     */
    void updateProfile(Long userId, ProfileUpdateRequest profile);

    /**
     * 获取我发起的申请列表（首页预览）
     *
     * @param userId 用户ID
     * @return 分页申请列表
     */
    PageResult<?> getMyApplications(Long userId);

    /**
     * 获取我的待审批列表（首页预览）
     *
     * @param userId 用户ID
     * @return 分页待办列表
     */
    PageResult<?> getMyApprovals(Long userId);

}

package com.hrms.business.mycenter.service;

/**
 * 个人中心服务接口
 */
public interface MyCenterService {

    /**
     * 获取个人信息
     *
     * @param userId 用户ID
     * @return 个人信息
     */
    Object getProfile(Long userId);

    /**
     * 更新个人信息
     *
     * @param userId  用户ID
     * @param profile 个人信息
     */
    void updateProfile(Long userId, Object profile);

    /**
     * 获取我的申请列表
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    Object getMyApplications(Long userId);

    /**
     * 获取我的审批列表
     *
     * @param userId 用户ID
     * @return 审批列表
     */
    Object getMyApprovals(Long userId);

}

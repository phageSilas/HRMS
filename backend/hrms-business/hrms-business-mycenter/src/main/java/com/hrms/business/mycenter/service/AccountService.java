package com.hrms.business.mycenter.service;

import com.hrms.business.mycenter.dto.LoginLogVO;
import com.hrms.business.mycenter.dto.PasswordChangeRequest;
import com.hrms.business.mycenter.dto.PhoneBindRequest;

import java.util.List;

/**
 * 账号安全服务接口
 */
public interface AccountService {

    /**
     * 修改密码
     *
     * @param userId  用户ID
     * @param request 密码修改请求
     */
    void changePassword(Long userId, PasswordChangeRequest request);

    /**
     * 绑定手机号
     *
     * @param userId  用户ID
     * @param request 手机绑定请求
     */
    void bindPhone(Long userId, PhoneBindRequest request);

    /**
     * 查询登录日志
     *
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<LoginLogVO> getLoginLogs(Long userId);
}

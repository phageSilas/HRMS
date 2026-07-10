package com.hrms.system.auth.service;

import com.hrms.system.auth.dto.LoginRequestDTO;
import com.hrms.system.auth.vo.LoginVO;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录响应（Token 和用户信息）
     */
    LoginVO login(LoginRequestDTO request);

    /**
     * 获取当前登录用户信息。
     *
     * @return 用户信息
     */
    LoginVO getCurrentUser();

    /**
     * 用户退出登录。
     */
    void logout();
}
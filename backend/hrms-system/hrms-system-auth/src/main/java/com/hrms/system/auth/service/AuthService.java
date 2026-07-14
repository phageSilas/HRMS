package com.hrms.system.auth.service;

import com.hrms.system.auth.vo.CurrentUserVO;
import com.hrms.system.auth.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return LoginVO
     */
    LoginVO login(String username, String password);

    /**
     * 用户登出
     *
     * @param token Token
     */
    void logout(String token);

    /**
     * 验证 Token
     *
     * @param token Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取当前用户信息
     *
     * @return CurrentUserVO
     */
    CurrentUserVO getCurrentUser();

}

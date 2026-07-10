package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.LoginRequestDTO;
import com.hrms.system.auth.service.AuthService;
import com.hrms.system.auth.util.PasswordUtils;
import com.hrms.system.auth.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 生成BCrypt密码hash (临时接口,仅用于测试)
     *
     * @param password 原始密码
     * @return BCrypt hash
     */
    @GetMapping("/gen-password")
    public Result<String> generatePassword(@RequestParam String password) {
        String hash = PasswordUtils.encode(password);
        return Result.success(hash);
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequestDTO request) {
        LoginVO vo = authService.login(request);
        return Result.success(vo);
    }

    /**
     * 获取当前用户信息。
     *
     * @return 用户信息
     */
    @GetMapping("/current-user")
    public Result<LoginVO> getCurrentUser() {
        LoginVO vo = authService.getCurrentUser();
        return Result.success(vo);
    }

    /**
     * 退出登录。
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
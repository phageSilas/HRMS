package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.LoginRequestDTO;
import com.hrms.system.auth.service.AuthService;
import com.hrms.system.auth.vo.CurrentUserVO;
import com.hrms.system.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证接口", description = "用户登录、登出等认证相关接口")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginVO loginVO = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出并失效当前Token")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public Result<CurrentUserVO> getCurrentUser() {
        CurrentUserVO currentUser = authService.getCurrentUser();
        return Result.success(currentUser);
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

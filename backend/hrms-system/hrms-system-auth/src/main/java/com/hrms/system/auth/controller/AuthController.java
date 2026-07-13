package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证接口", description = "用户登录、登出等认证相关接口")
public class AuthController {

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<Object> getCurrentUser() {
        return Result.success();
    }

}

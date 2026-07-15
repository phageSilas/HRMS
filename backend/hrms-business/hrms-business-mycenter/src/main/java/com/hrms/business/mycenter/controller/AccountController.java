package com.hrms.business.mycenter.controller;

import com.hrms.business.mycenter.dto.LoginLogVO;
import com.hrms.business.mycenter.dto.PasswordChangeRequest;
import com.hrms.business.mycenter.dto.PhoneBindRequest;
import com.hrms.business.mycenter.service.AccountService;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账号安全控制器
 * API-MYC-15 ~ 17
 */
@RestController
@RequestMapping("/api/v1/account")
@Tag(name = "账号安全", description = "修改密码、绑定手机、登录日志")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "旧密码验证后设置新密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        accountService.changePassword(userId, request);
        return Result.success();
    }

    @PostMapping("/phone/bind")
    @Operation(summary = "绑定手机", description = "绑定新手机号（需短信验证码）")
    public Result<Void> bindPhone(@Valid @RequestBody PhoneBindRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        accountService.bindPhone(userId, request);
        return Result.success();
    }

    @GetMapping("/login-logs")
    @Operation(summary = "登录日志", description = "登录历史记录")
    public Result<List<LoginLogVO>> getLoginLogs() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(accountService.getLoginLogs(userId));
    }
}

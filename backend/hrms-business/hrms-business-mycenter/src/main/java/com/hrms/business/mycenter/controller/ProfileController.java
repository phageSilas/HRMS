package com.hrms.business.mycenter.controller;

import com.hrms.business.mycenter.dto.ProfileUpdateRequest;
import com.hrms.business.mycenter.dto.ProfileVO;
import com.hrms.business.mycenter.service.ProfileService;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人档案控制器
 * API-MYC-01 ~ 02
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "个人档案", description = "查看/编辑个人档案信息")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "获取我的档案", description = "查看个人基本信息（已脱敏），附带字段权限控制")
    public Result<ProfileVO> getProfile() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(profileService.getProfile(userId));
    }

    @PutMapping
    @Operation(summary = "更新我的档案", description = "更新允许员工自行修改的字段")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        profileService.updateProfile(userId, request);
        return Result.success();
    }
}

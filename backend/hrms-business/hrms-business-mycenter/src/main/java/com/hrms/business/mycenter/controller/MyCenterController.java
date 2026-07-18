package com.hrms.business.mycenter.controller;

import com.hrms.business.mycenter.dto.ProfileVO;
import com.hrms.business.mycenter.service.MyCenterService;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心首页聚合控制器
 * <p>
 * 提供个人中心首页所需的一站式聚合数据（个人档案 + 待审批 + 我的申请）。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mycenter")
@Tag(name = "个人中心首页", description = "首页聚合数据、个人信息概览")
@RequiredArgsConstructor
public class MyCenterController {

    private final MyCenterService myCenterService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人档案概览", description = "个人中心首页展示的员工基本信息")
    public Result<ProfileVO> getProfile() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(myCenterService.getProfile(userId));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "我发起的申请（首页预览）", description = "个人中心首页展示的最近申请列表")
    public Result<PageResult<?>> getMyApplications() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(myCenterService.getMyApplications(userId));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "我的待审批（首页预览）", description = "个人中心首页展示的待审批任务列表")
    public Result<PageResult<?>> getMyApprovals() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(myCenterService.getMyApprovals(userId));
    }

}

package com.hrms.business.mycenter.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 */
@RestController
@RequestMapping("/api/v1/mycenter")
@Tag(name = "个人中心", description = "个人信息、我的申请、我的审批等接口")
public class MyCenterController {

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<Object> getProfile() {
        return Result.success();
    }

}

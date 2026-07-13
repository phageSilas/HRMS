package com.hrms.business.approval.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 审批中心控制器
 */
@RestController
@RequestMapping("/api/v1/approval")
@Tag(name = "审批中心", description = "审批流程、审批任务、审批委托等接口")
public class ApprovalController {

    /**
     * 获取审批列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

}

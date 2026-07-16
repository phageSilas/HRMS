package com.hrms.business.approval.controller;

import com.hrms.business.approval.dto.PendingTaskQuery;
import com.hrms.business.approval.dto.PendingTaskVO;
import com.hrms.business.approval.service.ApprovalTaskService;
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
 * 审批实例控制器
 * <p>
 * API-APR-03：我发起的申请
 * </p>
 */
@RestController
@RequestMapping("/api/v1/approval")
@Tag(name = "审批实例", description = "我发起的申请")
@RequiredArgsConstructor
public class ApprovalInstanceController {

    private final ApprovalTaskService approvalTaskService;

    @Operation(summary = "我发起的申请")
    @GetMapping("/my-applications")
    public Result<PageResult<PendingTaskVO>> getMyApplications(PendingTaskQuery query) {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(approvalTaskService.findMyApplications(userId, query));
    }
}

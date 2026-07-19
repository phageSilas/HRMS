package com.hrms.business.approval.controller;

import com.hrms.business.approval.dto.ApprovalDetailVO;
import com.hrms.business.approval.dto.ApprovalOperateRequest;
import com.hrms.business.approval.dto.OperateResultVO;
import com.hrms.business.approval.dto.PendingTaskQuery;
import com.hrms.business.approval.dto.PendingTaskVO;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 审批任务控制器
 * <p>
 * API-APR-01/02/04/05/06
 * </p>
 */
@RestController
@RequestMapping("/api/v1/approval")
@Tag(name = "审批任务", description = "待办、已办、详情、操作、撤回")
@RequiredArgsConstructor
public class ApprovalTaskController {

    private final ApprovalTaskService approvalTaskService;
    private final ApprovalEngine approvalEngine;

    @Operation(summary = "待办列表")
    @GetMapping("/tasks/pending")
    public Result<PageResult<PendingTaskVO>> getPendingTasks(PendingTaskQuery query) {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(approvalTaskService.findPendingTasks(userId, query));
    }

    @Operation(summary = "已审批列表")
    @GetMapping("/tasks/history")
    public Result<PageResult<PendingTaskVO>> getHistoryTasks(PendingTaskQuery query) {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(approvalTaskService.findHistoryTasks(userId, query));
    }

    @Operation(summary = "任务列表（支持筛选类型：pending/today-approved/overdue）")
    @GetMapping("/tasks")
    public Result<PageResult<PendingTaskVO>> getTasks(PendingTaskQuery query) {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(approvalTaskService.findFilteredTasks(userId, query));
    }

    @Operation(summary = "审批详情")
    @GetMapping("/{id}")
    public Result<ApprovalDetailVO> getDetail(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(approvalTaskService.getDetail(id, userId));
    }

    @Operation(summary = "审批操作")
    @PostMapping("/{id}/operate")
    public Result<OperateResultVO> operate(@PathVariable Long id,
                                           @Valid @RequestBody ApprovalOperateRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        OperateResultVO result = approvalEngine.processAction(id, request.getAction(), request.getComment(), request.getTargetUserId());
        return Result.success(result);
    }

    @Operation(summary = "待审批数量（角标用）")
    @GetMapping("/pending-count")
    public Result<Map<String, Integer>> getPendingCount() {
        Long userId = SecurityContextHolder.getUserId();
        Integer count = approvalTaskService.getPendingCount(userId);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "今日已审批数量")
    @GetMapping("/today-approved-count")
    public Result<Map<String, Integer>> getTodayApprovedCount() {
        Long userId = SecurityContextHolder.getUserId();
        Integer count = approvalTaskService.getTodayApprovedCount(userId);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "已逾期数量")
    @GetMapping("/overdue-count")
    public Result<Map<String, Integer>> getOverdueCount() {
        Long userId = SecurityContextHolder.getUserId();
        Integer count = approvalTaskService.getOverdueCount(userId);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "撤回申请")
    @PostMapping("/{id}/withdraw")
    public Result<OperateResultVO> withdraw(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getUserId();
        approvalTaskService.withdraw(id, userId);
        return Result.success(OperateResultVO.ok());
    }
}

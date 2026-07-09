package com.hrms.business.approval.controller;

import com.hrms.business.approval.dto.ApprovalHandleRequestDTO;
import com.hrms.business.approval.dto.ApprovalStartRequestDTO;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.approval.vo.ApprovalTaskVO;
import com.hrms.common.model.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供审批中心 HTTP 接口。
 */
@RestController
@RequestMapping("/api/business/approval")
public class ApprovalTaskController {

    private final ApprovalTaskService approvalTaskService;

    /**
     * 创建审批中心控制器。
     *
     * @param approvalTaskService 审批中心业务服务
     */
    public ApprovalTaskController(ApprovalTaskService approvalTaskService) {
        this.approvalTaskService = approvalTaskService;
    }

    /**
     * 发起审批任务。
     *
     * @param requestParam 审批发起请求参数
     * @return 审批任务信息
     */
    @PostMapping("/start")
    public Result<ApprovalTaskVO> start(@Valid @RequestBody ApprovalStartRequestDTO requestParam) {
        return Result.success(approvalTaskService.start(requestParam));
    }

    /**
     * 查询审批任务详情。
     *
     * @param taskId 审批任务ID
     * @return 审批任务信息
     */
    @GetMapping("/tasks/{taskId}")
    public Result<ApprovalTaskVO> getTask(@PathVariable Long taskId) {
        return Result.success(approvalTaskService.getById(taskId));
    }

    /**
     * 通过审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     * @return 统一返回对象
     */
    @PatchMapping("/tasks/{taskId}/approve")
    public Result<Void> approve(@PathVariable Long taskId, @RequestBody ApprovalHandleRequestDTO requestParam) {
        approvalTaskService.approve(taskId, requestParam);
        return Result.success();
    }

    /**
     * 驳回审批任务。
     *
     * @param taskId 审批任务ID
     * @param requestParam 审批处理请求参数
     * @return 统一返回对象
     */
    @PatchMapping("/tasks/{taskId}/reject")
    public Result<Void> reject(@PathVariable Long taskId, @RequestBody ApprovalHandleRequestDTO requestParam) {
        approvalTaskService.reject(taskId, requestParam);
        return Result.success();
    }
}

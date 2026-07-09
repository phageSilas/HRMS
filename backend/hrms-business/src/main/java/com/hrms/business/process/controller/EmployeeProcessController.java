package com.hrms.business.process.controller;

import com.hrms.business.process.dto.EmployeeProcessStartRequestDTO;
import com.hrms.business.process.service.EmployeeProcessService;
import com.hrms.business.process.vo.EmployeeProcessVO;
import com.hrms.common.model.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供员工入转调离流程 HTTP 接口。
 */
@RestController
@RequestMapping("/api/business/process")
public class EmployeeProcessController {

    private final EmployeeProcessService employeeProcessService;

    /**
     * 创建员工流程控制器。
     *
     * @param employeeProcessService 员工流程业务服务
     */
    public EmployeeProcessController(EmployeeProcessService employeeProcessService) {
        this.employeeProcessService = employeeProcessService;
    }

    /**
     * 发起入职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @PostMapping("/onboarding")
    public Result<EmployeeProcessVO> startOnboarding(@Valid @RequestBody EmployeeProcessStartRequestDTO requestParam) {
        return Result.success(employeeProcessService.startOnboarding(requestParam));
    }

    /**
     * 发起转正流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @PostMapping("/regularization")
    public Result<EmployeeProcessVO> startRegularization(@Valid @RequestBody EmployeeProcessStartRequestDTO requestParam) {
        return Result.success(employeeProcessService.startRegularization(requestParam));
    }

    /**
     * 发起调岗流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @PostMapping("/transfer")
    public Result<EmployeeProcessVO> startTransfer(@Valid @RequestBody EmployeeProcessStartRequestDTO requestParam) {
        return Result.success(employeeProcessService.startTransfer(requestParam));
    }

    /**
     * 发起离职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @PostMapping("/resignation")
    public Result<EmployeeProcessVO> startResignation(@Valid @RequestBody EmployeeProcessStartRequestDTO requestParam) {
        return Result.success(employeeProcessService.startResignation(requestParam));
    }
}

package com.hrms.system.controller;

import com.hrms.common.web.Result;
import com.hrms.system.service.SystemModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供系统基础域的演示接口。
 */
@RestController
@RequestMapping("/api/system")
public class SystemModuleController {

    private final SystemModuleService systemModuleService;

    /**
     * 创建系统基础域控制器。
     *
     * @param systemModuleService 系统基础域服务
     */
    public SystemModuleController(SystemModuleService systemModuleService) {
        this.systemModuleService = systemModuleService;
    }

    /**
     * 获取系统基础域的模块信息。
     *
     * @return 统一返回对象
     */
    @GetMapping("/summary")
    public Result<String> getSummary() {
        return Result.success(systemModuleService.getModuleSummary());
    }
}

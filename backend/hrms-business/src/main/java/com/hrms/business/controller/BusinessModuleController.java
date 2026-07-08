package com.hrms.business.controller;

import com.hrms.business.service.BusinessModuleService;
import com.hrms.common.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供业务域的演示接口。
 */
@RestController
@RequestMapping("/api/business")
public class BusinessModuleController {

    private final BusinessModuleService businessModuleService;

    /**
     * 创建业务域控制器。
     *
     * @param businessModuleService 业务域服务
     */
    public BusinessModuleController(BusinessModuleService businessModuleService) {
        this.businessModuleService = businessModuleService;
    }

    /**
     * 获取业务域的模块信息。
     *
     * @return 统一返回对象
     */
    @GetMapping("/summary")
    public Result<String> getSummary() {
        return Result.success(businessModuleService.getModuleSummary());
    }
}

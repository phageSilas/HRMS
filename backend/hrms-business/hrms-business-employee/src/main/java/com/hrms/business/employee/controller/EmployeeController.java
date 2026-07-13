package com.hrms.business.employee.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "员工接口", description = "员工档案管理相关接口")
public class EmployeeController {

    /**
     * 获取员工列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<Object> get(Long id) {
        return Result.success();
    }

}

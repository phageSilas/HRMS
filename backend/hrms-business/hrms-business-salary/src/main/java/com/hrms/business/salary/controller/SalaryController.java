package com.hrms.business.salary.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 薪资管理控制器
 */
@RestController
@RequestMapping("/api/v1/salary")
@Tag(name = "薪资管理", description = "薪资核算、薪资批次、工资条等接口")
public class SalaryController {

    /**
     * 获取薪资记录列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

}

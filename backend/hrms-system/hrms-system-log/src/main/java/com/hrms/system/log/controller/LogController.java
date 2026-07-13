package com.hrms.system.log.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 日志审计控制器
 */
@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "日志审计", description = "操作日志、登录日志等审计接口")
public class LogController {

    /**
     * 获取日志列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

}

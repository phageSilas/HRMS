package com.hrms.system.file.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 附件管理控制器
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "附件管理", description = "文件上传、下载、管理等接口")
public class FileController {

    /**
     * 获取文件列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

}

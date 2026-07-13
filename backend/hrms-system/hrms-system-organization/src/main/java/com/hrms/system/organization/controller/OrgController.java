package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织控制器
 */
@RestController
@RequestMapping("/api/v1/org")
@Tag(name = "组织接口", description = "部门、职位等组织相关接口")
public class OrgController {

    /**
     * 获取部门树
     */
    @GetMapping("/dept-tree")
    public Result<Object> getDeptTree() {
        return Result.success();
    }

}

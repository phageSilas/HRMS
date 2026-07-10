package com.hrms.system.auth.controller;

import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import com.hrms.system.auth.service.PermissionService;
import com.hrms.system.auth.vo.DataScopeVO;
import com.hrms.system.auth.vo.FieldPermissionVO;
import com.hrms.system.auth.vo.PermissionVO;
import org.springframework.web.bind.annotation.*;

/**
 * 权限控制控制器。
 */
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 获取当前用户权限列表。
     */
    @GetMapping
    public Result<PermissionVO> getUserPermissions() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            return Result.failure(40100, "用户未登录");
        }

        PermissionVO vo = permissionService.getUserPermissions(userId);
        return Result.success(vo);
    }

    /**
     * 获取当前用户数据权限范围。
     */
    @GetMapping("/data-scope")
    public Result<DataScopeVO> getDataScope() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            return Result.failure(40100, "用户未登录");
        }

        DataScopeVO vo = permissionService.getDataScope(userId);
        return Result.success(vo);
    }

    /**
     * 获取当前用户字段权限。
     */
    @GetMapping("/field")
    public Result<FieldPermissionVO> getFieldPermissions(@RequestParam(required = false) String module) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            return Result.failure(40100, "用户未登录");
        }

        FieldPermissionVO vo = permissionService.getFieldPermissions(userId, module);
        return Result.success(vo);
    }
}
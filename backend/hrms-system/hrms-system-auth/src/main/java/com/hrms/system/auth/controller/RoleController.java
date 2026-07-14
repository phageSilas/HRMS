package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "角色管理", description = "角色的增删改查接口")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色")
    public Result<Long> create(@RequestBody RoleEntity role) {
        Long id = roleService.create(role);
        return Result.success(id);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "根据ID更新角色信息")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoleEntity role) {
        role.setId(id);
        roleService.update(role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询角色详情", description = "根据ID查询角色详情")
    public Result<RoleEntity> getById(@PathVariable Long id) {
        RoleEntity role = roleService.getById(id);
        return Result.success(role);
    }

    /**
     * 查询所有角色
     */
    @GetMapping
    @Operation(summary = "查询角色列表", description = "查询所有角色列表")
    public Result<List<RoleEntity>> list() {
        List<RoleEntity> roles = roleService.list();
        return Result.success(roles);
    }

}

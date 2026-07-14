package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.entity.MenuEntity;
import com.hrms.system.auth.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/v1/menus")
@Tag(name = "菜单管理", description = "菜单的增删改查接口")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 创建菜单
     */
    @PostMapping
    @Operation(summary = "创建菜单", description = "创建新菜单")
    public Result<Long> create(@RequestBody MenuEntity menu) {
        Long id = menuService.create(menu);
        return Result.success(id);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单", description = "根据ID更新菜单信息")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuEntity menu) {
        menu.setId(id);
        menuService.update(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 查询菜单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询菜单详情", description = "根据ID查询菜单详情")
    public Result<MenuEntity> getById(@PathVariable Long id) {
        MenuEntity menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 查询所有菜单
     */
    @GetMapping
    @Operation(summary = "查询菜单列表", description = "查询所有菜单列表")
    public Result<List<MenuEntity>> list() {
        List<MenuEntity> menus = menuService.list();
        return Result.success(menus);
    }

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "查询菜单树", description = "查询菜单树形结构")
    public Result<List<MenuEntity>> tree() {
        List<MenuEntity> menus = menuService.buildMenuTree();
        return Result.success(menus);
    }

}

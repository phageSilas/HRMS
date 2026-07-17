package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.RoleMenuAssignDTO;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.vo.RoleVO;
import com.hrms.system.log.annotation.OperateLog;
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
    @OperateLog(title = "角色管理", businessType = "INSERT")
    public Result<Long> create(@RequestBody RoleEntity role) {
        Long id = roleService.create(role);
        return Result.success(id);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "根据ID更新角色信息")
    @OperateLog(title = "角色管理", businessType = "UPDATE")
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
    @OperateLog(title = "角色管理", businessType = "DELETE")
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
     * 查询所有角色（返回RoleVO，包含菜单ID）
     */
    @GetMapping
    @Operation(summary = "查询角色列表", description = "查询所有角色列表，支持按角色名称搜索，包含关联的菜单ID")
    public Result<List<RoleVO>> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer status) {
        List<RoleVO> roles = roleService.listRoleVOs(keyword, status);
        return Result.success(roles);
    }

    /**
     * 分配角色菜单权限
     */
    @PostMapping("/{roleId}/menus")
    @Operation(summary = "分配角色菜单权限", description = "为角色分配菜单权限，全量覆盖")
    @OperateLog(title = "角色管理", businessType = "UPDATE")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody RoleMenuAssignDTO assignDTO) {
        roleService.assignMenus(roleId, assignDTO);
        return Result.success();
    }

}

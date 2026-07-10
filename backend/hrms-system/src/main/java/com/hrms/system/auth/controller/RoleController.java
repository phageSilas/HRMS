package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.RoleCreateDTO;
import com.hrms.system.auth.dto.RoleQueryDTO;
import com.hrms.system.auth.dto.RoleUpdateDTO;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.vo.RoleVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器。
 */
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody RoleCreateDTO dto) {
        Long id = roleService.create(dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoleUpdateDTO dto) {
        dto.setId(id);
        roleService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        RoleVO vo = roleService.getById(id);
        return Result.success(vo);
    }

    @GetMapping
    public Result<List<RoleVO>> list(RoleQueryDTO dto) {
        List<RoleVO> list = roleService.list(dto);
        return Result.success(list);
    }

    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }

    @PostMapping("/{id}/users")
    public Result<Void> assignUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        roleService.assignUsers(id, userIds);
        return Result.success();
    }
}
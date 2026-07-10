package com.hrms.system.auth.controller;

import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.MenuCreateDTO;
import com.hrms.system.auth.dto.MenuQueryDTO;
import com.hrms.system.auth.dto.MenuUpdateDTO;
import com.hrms.system.auth.service.MenuService;
import com.hrms.system.auth.vo.MenuTreeVO;
import com.hrms.system.auth.vo.MenuVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器。
 */
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 创建菜单。
     */
    @PostMapping
    public Result<Long> create(@RequestBody MenuCreateDTO dto) {
        Long id = menuService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新菜单。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuUpdateDTO dto) {
        dto.setId(id);
        menuService.update(dto);
        return Result.success();
    }

    /**
     * 删除菜单。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 查询菜单详情。
     */
    @GetMapping("/{id}")
    public Result<MenuVO> getById(@PathVariable Long id) {
        MenuVO vo = menuService.getById(id);
        return Result.success(vo);
    }

    /**
     * 查询菜单列表。
     */
    @GetMapping
    public Result<List<MenuVO>> list(MenuQueryDTO dto) {
        List<MenuVO> list = menuService.list(dto);
        return Result.success(list);
    }

    /**
     * 查询菜单树。
     */
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        List<MenuTreeVO> tree = menuService.tree();
        return Result.success(tree);
    }

    /**
     * 获取当前用户的菜单树。
     */
    @GetMapping("/user-tree")
    public Result<List<MenuTreeVO>> getUserMenuTree() {
        List<MenuTreeVO> tree = menuService.getUserMenuTree();
        return Result.success(tree);
    }
}
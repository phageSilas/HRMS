package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptTreeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器。
 */
@RestController
@RequestMapping("/departments")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 创建部门。
     */
    @PostMapping
    public Result<Long> create(@RequestBody DeptCreateDTO dto) {
        Long id = deptService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新部门。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeptCreateDTO dto) {
        deptService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除部门。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }

    /**
     * 查询部门树。
     */
    @GetMapping("/tree")
    public Result<List<DeptTreeVO>> tree() {
        List<DeptTreeVO> tree = deptService.tree();
        return Result.success(tree);
    }

    /**
     * 查询部门详情。
     */
    @GetMapping("/{id}")
    public Result<DeptTreeVO> getById(@PathVariable Long id) {
        DeptTreeVO vo = deptService.getById(id);
        return Result.success(vo);
    }
}
package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.dto.DeptUpdateDTO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.DeptListVO;
import com.hrms.system.organization.vo.DeptTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/api/v1/depts")
@Tag(name = "部门管理", description = "部门的增删改查接口")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    @Operation(summary = "部门树查询", description = "获取完整的部门层级树结构")
    public Result<List<DeptTreeVO>> getDeptTree() {
        List<DeptTreeVO> tree = deptService.getDeptTree();
        return Result.success(tree);
    }

    /**
     * 获取部门平铺列表
     */
    @GetMapping
    @Operation(summary = "部门平铺列表", description = "获取不含嵌套结构的部门列表")
    public Result<List<DeptListVO>> getDeptList() {
        List<DeptListVO> list = deptService.getDeptList();
        return Result.success(list);
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "部门详情查询", description = "根据ID查询部门详情")
    public Result<DeptDetailVO> getById(@PathVariable Long id) {
        DeptDetailVO detail = deptService.getDeptById(id);
        return Result.success(detail);
    }

    /**
     * 创建部门
     */
    @PostMapping
    @Operation(summary = "创建部门", description = "创建新部门，自动计算层级和祖级路径")
    public Result<Long> create(@Valid @RequestBody DeptCreateDTO createDTO) {
        Long id = deptService.createDept(createDTO);
        return Result.success(id);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "根据ID更新部门信息，deptCode不可修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateDTO updateDTO) {
        deptService.updateDept(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "逻辑删除部门，需满足无子部门且无在职员工")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }

}

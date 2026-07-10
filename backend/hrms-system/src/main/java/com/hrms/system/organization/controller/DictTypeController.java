package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.dto.DictTypeQueryDTO;
import com.hrms.system.organization.dto.DictTypeUpdateDTO;
import com.hrms.system.organization.service.DictTypeService;
import com.hrms.system.organization.vo.DictTypeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型管理控制器。
 */
@RestController
@RequestMapping("/dict-types")
public class DictTypeController {

    private final DictTypeService dictTypeService;

    public DictTypeController(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody DictTypeCreateDTO dto) {
        Long id = dictTypeService.create(dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DictTypeUpdateDTO dto) {
        dto.setId(id);
        dictTypeService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DictTypeVO> getById(@PathVariable Long id) {
        DictTypeVO vo = dictTypeService.getById(id);
        return Result.success(vo);
    }

    @GetMapping
    public Result<List<DictTypeVO>> list(DictTypeQueryDTO dto) {
        List<DictTypeVO> list = dictTypeService.list(dto);
        return Result.success(list);
    }
}
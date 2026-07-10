package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictDataQueryDTO;
import com.hrms.system.organization.dto.DictDataUpdateDTO;
import com.hrms.system.organization.service.DictDataService;
import com.hrms.system.organization.vo.DictDataVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理控制器。
 */
@RestController
@RequestMapping("/dict-data")
public class DictDataController {

    private final DictDataService dictDataService;

    public DictDataController(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody DictDataCreateDTO dto) {
        Long id = dictDataService.create(dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DictDataUpdateDTO dto) {
        dto.setId(id);
        dictDataService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DictDataVO> getById(@PathVariable Long id) {
        DictDataVO vo = dictDataService.getById(id);
        return Result.success(vo);
    }

    @GetMapping
    public Result<List<DictDataVO>> list(DictDataQueryDTO dto) {
        List<DictDataVO> list = dictDataService.list(dto);
        return Result.success(list);
    }

    @GetMapping("/type/{typeCode}")
    public Result<List<DictDataVO>> getByTypeCode(@PathVariable String typeCode) {
        List<DictDataVO> list = dictDataService.getByTypeCode(typeCode);
        return Result.success(list);
    }
}
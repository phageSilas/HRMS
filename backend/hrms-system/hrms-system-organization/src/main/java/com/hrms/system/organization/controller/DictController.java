package com.hrms.system.organization.controller;

import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.DictDataCreateDTO;
import com.hrms.system.organization.dto.DictTypeCreateDTO;
import com.hrms.system.organization.service.DictService;
import com.hrms.system.organization.vo.DictDataVO;
import com.hrms.system.organization.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 字典管理控制器
 */
@RestController
@RequestMapping("/api/v1/dicts")
@Tag(name = "字典管理", description = "字典类型和字典数据的增删改查接口")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 字典类型列表
     */
    @GetMapping("/types")
    @Operation(summary = "字典类型列表", description = "分页查询字典类型列表")
    public Result<PageResult<DictTypeVO>> listTypes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<DictTypeVO> pageResult = dictService.listDictTypes(pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 创建字典类型
     */
    @PostMapping("/types")
    @Operation(summary = "创建字典类型", description = "创建新的字典类型")
    public Result<Long> createType(@Valid @RequestBody DictTypeCreateDTO createDTO) {
        Long id = dictService.createDictType(createDTO);
        return Result.success(id);
    }

    /**
     * 字典数据列表
     */
    @GetMapping("/data/{typeCode}")
    @Operation(summary = "字典数据列表", description = "根据字典类型编码查询字典数据列表")
    public Result<List<DictDataVO>> getDataByType(@PathVariable String typeCode) {
        List<DictDataVO> list = dictService.getDictDataByType(typeCode);
        return Result.success(list);
    }

    /**
     * 创建字典数据
     */
    @PostMapping("/data")
    @Operation(summary = "创建字典数据", description = "在指定字典类型下创建字典数据项")
    public Result<Long> createData(@Valid @RequestBody DictDataCreateDTO createDTO) {
        Long id = dictService.createDictData(createDTO);
        return Result.success(id);
    }

}

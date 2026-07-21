package com.hrms.business.employee.controller;

import com.hrms.business.employee.dto.ContractCreateDTO;
import com.hrms.business.employee.dto.ContractUpdateDTO;
import com.hrms.business.employee.entity.EmployeeContractEntity;
import com.hrms.business.employee.service.EmployeeContractService;
import com.hrms.business.employee.vo.EmployeeContractDetailVO;
import com.hrms.business.employee.vo.EmployeeContractVO;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工合同控制器
 */
@RestController
@RequestMapping("/api/v1/employee-contracts")
@Tag(name = "员工合同接口", description = "员工合同管理相关接口")
@RequiredArgsConstructor
public class EmployeeContractController {

    private final EmployeeContractService contractService;

    /**
     * 创建合同
     */
    @PostMapping
    @Operation(summary = "创建合同", description = "为员工创建新合同")
    public Result<EmployeeContractEntity> create(@RequestBody @Valid ContractCreateDTO createDTO) {
        EmployeeContractEntity contract = contractService.createContract(createDTO);
        return Result.success(contract);
    }

    /**
     * 更新合同
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新合同", description = "更新合同信息")
    public Result<EmployeeContractEntity> update(
            @PathVariable Long id,
            @RequestBody ContractUpdateDTO updateDTO) {
        EmployeeContractEntity contract = contractService.updateContract(id, updateDTO);
        return Result.success(contract);
    }

    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除合同", description = "逻辑删除合同")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.deleteContract(id);
        return Result.success();
    }

    /**
     * 合同详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "合同详情", description = "获取合同详细信息")
    public Result<EmployeeContractVO> get(@PathVariable Long id) {
        EmployeeContractVO vo = contractService.getContractDetail(id);
        return Result.success(vo);
    }

    /**
     * 员工合同列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "员工合同列表", description = "获取指定员工的所有合同")
    public Result<List<EmployeeContractVO>> listByEmployee(
            @Parameter(description = "员工ID") @PathVariable Long employeeId) {
        List<EmployeeContractVO> list = contractService.getContractsByEmployee(employeeId);
        return Result.success(list);
    }

    /**
     * 所有合同列表（分页）
     */
    @GetMapping("/all")
    @Operation(summary = "所有合同列表", description = "获取所有合同列表（分页），支持按员工姓名、工号、合同编号搜索")
    public Result<Map<String, Object>> listAll(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        List<EmployeeContractDetailVO> list = contractService.getAllContracts(pageNum, pageSize, keyword);
        long total = contractService.getAllContractsCount(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        return Result.success(result);
    }

}

package com.hrms.business.employee.controller;

import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.dto.EmployeeQueryDTO;
import com.hrms.business.employee.dto.EmployeeUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.employee.vo.EmployeeDetailVO;
import com.hrms.business.employee.vo.EmployeeGenNoVO;
import com.hrms.business.employee.vo.EmployeeListVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "员工接口", description = "员工档案管理相关接口")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * API-EMP-01：员工列表（分页查询）
     */
    @GetMapping
    @Operation(summary = "员工列表（分页查询）", description = "分页查询员工列表，支持高级搜索")
    public Result<PageResult<EmployeeListVO>> list(EmployeeQueryDTO queryDTO) {
        PageResult<EmployeeListVO> result = employeeService.listEmployees(queryDTO);
        return Result.success(result);
    }

    /**
     * API-EMP-02：员工详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "员工详情", description = "获取员工完整档案")
    public Result<EmployeeDetailVO> get(@PathVariable Long id) {
        EmployeeDetailVO detail = employeeService.getEmployeeDetail(id);
        return Result.success(detail);
    }

    /**
     * API-EMP-03：新增员工
     */
    @PostMapping
    @Operation(summary = "新增员工", description = "新增员工（直接录入）")
    public Result<EmployeeEntity> create(@RequestBody @Valid EmployeeCreateDTO createDTO) {
        EmployeeEntity employee = employeeService.createEmployee(createDTO);
        return Result.success(employee);
    }

    /**
     * API-EMP-04：全量更新员工
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新员工", description = "全量更新员工主档")
    public Result<EmployeeEntity> update(@PathVariable Long id, @RequestBody EmployeeUpdateDTO updateDTO) {
        EmployeeEntity employee = employeeService.updateEmployee(id, updateDTO);
        return Result.success(employee);
    }

    /**
     * API-EMP-05：部分更新员工
     */
    @PatchMapping("/{id}")
    @Operation(summary = "部分更新员工", description = "部分字段更新/状态变更")
    public Result<EmployeeEntity> patch(@PathVariable Long id, @RequestBody EmployeeUpdateDTO updateDTO) {
        EmployeeEntity employee = employeeService.patchEmployee(id, updateDTO);
        return Result.success(employee);
    }

    /**
     * API-EMP-06：删除员工
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工", description = "逻辑删除（is_deleted=1）")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * API-EMP-07：生成工号
     */
    @GetMapping("/gen-no")
    @Operation(summary = "生成工号", description = "生成新的员工工号")
    public Result<EmployeeGenNoVO> generateEmployeeNo(@RequestParam String deptCode) {
        EmployeeGenNoVO genNoVO = employeeService.generateEmployeeNo(deptCode);
        return Result.success(genNoVO);
    }

    /**
     * API-EMP-08：检查部门下是否有在职员工
     */
    @GetMapping("/check-dept")
    @Operation(summary = "检查部门是否有在职员工", description = "删除部门前调用此接口校验")
    @Parameter(name = "deptId", description = "部门ID", required = true)
    public Result<Boolean> hasEmployeesInDept(@RequestParam Long deptId) {
        boolean hasEmployees = employeeService.hasEmployeesInDept(deptId);
        return Result.success(hasEmployees);
    }

    /**
     * API-EMP-09：检查职位下是否有在职员工
     */
    @GetMapping("/check-post")
    @Operation(summary = "检查职位是否有在职员工", description = "删除职位前调用此接口校验")
    @Parameter(name = "postId", description = "职位ID", required = true)
    public Result<Boolean> hasEmployeesInPost(@RequestParam Long postId) {
        boolean hasEmployees = employeeService.hasEmployeesInPost(postId);
        return Result.success(hasEmployees);
    }

}

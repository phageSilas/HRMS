package com.hrms.business.salary.controller;

import com.hrms.business.salary.dto.EmployeeSalaryProfileRequestDTO;
import com.hrms.business.salary.service.SalaryService;
import com.hrms.business.salary.vo.EmployeeSalaryProfileVO;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工薪资档案控制器。
 */
@RestController
@RequestMapping("/api/v1/salary/employees")
@Tag(name = "薪资档案", description = "员工薪资档案查询和设置接口")
@RequiredArgsConstructor
public class SalaryProfileController {

    private final SalaryService salaryService;

    /**
     * 查询员工薪资档案。
     *
     * @param employeeId 员工ID
     * @return 薪资档案
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/{employeeId}/profile")
    public Result<EmployeeSalaryProfileVO> getEmployeeProfile(@PathVariable("employeeId") Long employeeId) {
        return Result.success(salaryService.getEmployeeProfile(employeeId));
    }

    /**
     * 设置员工薪资档案。
     *
     * @param employeeId 员工ID
     * @param requestDTO 设置请求
     * @return 设置后的薪资档案
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PutMapping("/{employeeId}/profile")
    public Result<EmployeeSalaryProfileVO> setEmployeeProfile(
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody EmployeeSalaryProfileRequestDTO requestDTO) {
        return Result.success(salaryService.setEmployeeProfile(employeeId, requestDTO));
    }
}

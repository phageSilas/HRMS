package com.hrms.business.profile.controller;

import com.hrms.business.profile.dto.EmployeeNoGenerateRequestDTO;
import com.hrms.business.profile.dto.EmployeeQueryDTO;
import com.hrms.business.profile.service.EmployeeProfileService;
import com.hrms.business.profile.vo.EmployeeBriefVO;
import com.hrms.business.profile.vo.EmployeeNoVO;
import com.hrms.common.model.PageResult;
import com.hrms.common.model.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供员工档案管理 HTTP 接口。
 */
@RestController
@RequestMapping("/api/business/employees")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    /**
     * 创建员工档案控制器。
     *
     * @param employeeProfileService 员工档案业务服务
     */
    public EmployeeProfileController(EmployeeProfileService employeeProfileService) {
        this.employeeProfileService = employeeProfileService;
    }

    /**
     * 获取员工简要信息。
     *
     * @param id 员工ID
     * @return 员工简要信息
     */
    @GetMapping("/brief/{id}")
    public Result<EmployeeBriefVO> getBrief(@PathVariable Long id) {
        return Result.success(employeeProfileService.getBriefById(id));
    }

    /**
     * 生成员工工号。
     *
     * @param requestParam 工号生成请求参数
     * @return 工号生成结果
     */
    @PostMapping("/gen-no")
    public Result<EmployeeNoVO> generateEmployeeNo(@Valid @RequestBody EmployeeNoGenerateRequestDTO requestParam) {
        return Result.success(employeeProfileService.generateEmployeeNo(requestParam));
    }

    /**
     * 分页查询员工档案。
     *
     * @param queryParam 员工档案查询参数
     * @return 员工档案分页结果
     */
    @PostMapping("/page")
    public Result<PageResult<EmployeeBriefVO>> pageEmployees(@Valid @RequestBody EmployeeQueryDTO queryParam) {
        return Result.success(employeeProfileService.pageEmployees(queryParam));
    }
}

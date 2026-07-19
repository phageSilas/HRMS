package com.hrms.business.salary.controller;

import com.hrms.business.salary.dto.SalaryTemplateCreateOrUpdateRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateQueryDTO;
import com.hrms.business.salary.service.SalaryTemplateService;
import com.hrms.business.salary.vo.SalaryTemplatePageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 薪资账套控制器。
 */
@RestController
@RequestMapping("/api/v1/salary/templates")
@Tag(name = "薪资账套", description = "薪资账套分页、新增和更新接口")
@RequiredArgsConstructor
public class SalaryTemplateController {

    private final SalaryTemplateService salaryTemplateService;

    /**
     * 分页查询薪资账套。
     *
     * @param queryDTO 查询参数
     * @return 薪资账套分页结果
     * 本方法使用的工具类: Result(hrms-common),PageResult(hrms-common)
     */
    @GetMapping
    public Result<PageResult<SalaryTemplatePageVO>> pageTemplates(@Valid SalaryTemplateQueryDTO queryDTO) {
        return Result.success(salaryTemplateService.pageTemplates(queryDTO));
    }

    /**
     * 创建薪资账套。
     *
     * @param requestDTO 创建请求
     * @return 创建后的薪资账套
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping
    public Result<SalaryTemplatePageVO> createTemplate(
            @Valid @RequestBody SalaryTemplateCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(salaryTemplateService.createTemplate(requestDTO));
    }

    /**
     * 更新薪资账套。
     *
     * @param id         账套ID
     * @param requestDTO 更新请求
     * @return 更新后的薪资账套
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PutMapping("/{id}")
    public Result<SalaryTemplatePageVO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody SalaryTemplateCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(salaryTemplateService.updateTemplate(id, requestDTO));
    }
}

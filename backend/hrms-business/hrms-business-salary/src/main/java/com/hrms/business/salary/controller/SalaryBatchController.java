package com.hrms.business.salary.controller;

import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.service.SalaryService;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchVO;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 薪资核算批次控制器。
 */
@RestController
@RequestMapping("/api/v1/salary/batches")
@Tag(name = "薪资批次", description = "薪资批次创建、核算、预览和审批提交接口")
@RequiredArgsConstructor
public class SalaryBatchController {

    private final SalaryService salaryService;

    /**
     * 创建薪资批次。
     *
     * @param requestDTO 创建请求
     * @return 薪资批次
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping
    public Result<SalaryBatchVO> createBatch(@Valid @RequestBody SalaryBatchCreateRequestDTO requestDTO) {
        return Result.success(salaryService.createBatch(requestDTO));
    }

    /**
     * 按月份和核算范围查询当前薪资批次。
     *
     * @param salaryMonth 薪资月份
     * @param scopeType   核算范围类型
     * @param scopeValue  核算范围值
     * @return 当前薪资批次，未找到时为 null
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/current")
    public Result<SalaryBatchVO> getCurrentBatch(@RequestParam String salaryMonth,
                                                 @RequestParam(required = false) String scopeType,
                                                 @RequestParam(required = false) String scopeValue) {
        return Result.success(salaryService.getCurrentBatch(salaryMonth, scopeType, scopeValue));
    }

    /**
     * 触发薪资核算。
     *
     * @param id 批次ID
     * @return 核算后的批次
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/{id}/calculate")
    public Result<SalaryBatchVO> calculateBatch(@PathVariable Long id) {
        return Result.success(salaryService.calculateBatch(id));
    }

    /**
     * 预览薪资批次。
     *
     * @param id 批次ID
     * @return 薪资预览
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/{id}/preview")
    public Result<SalaryBatchPreviewVO> previewBatch(@PathVariable Long id) {
        return Result.success(salaryService.previewBatch(id));
    }

    /**
     * 提交薪资批次审批。
     *
     * @param id 批次ID
     * @return 提交后的批次
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/{id}/submit")
    public Result<SalaryBatchVO> submitBatch(@PathVariable Long id) {
        return Result.success(salaryService.submitBatch(id));
    }
}

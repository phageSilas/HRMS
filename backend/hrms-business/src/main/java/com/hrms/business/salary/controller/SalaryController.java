package com.hrms.business.salary.controller;

import com.hrms.business.salary.dto.SalaryMonthlyQueryDTO;
import com.hrms.business.salary.service.SalaryService;
import com.hrms.business.salary.vo.SalaryMonthlyVO;
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
 * 提供薪资管理 HTTP 接口。
 */
@RestController
@RequestMapping("/api/business/salary")
public class SalaryController {

    private final SalaryService salaryService;

    /**
     * 创建薪资控制器。
     *
     * @param salaryService 薪资业务服务
     */
    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    /**
     * 分页查询月度薪资。
     *
     * @param queryParam 月度薪资查询参数
     * @return 月度薪资分页结果
     */
    @PostMapping("/monthly/page")
    public Result<PageResult<SalaryMonthlyVO>> pageMonthlySalary(@Valid @RequestBody SalaryMonthlyQueryDTO queryParam) {
        return Result.success(salaryService.pageMonthlySalary(queryParam));
    }

    /**
     * 查询月度薪资详情。
     *
     * @param id 月度薪资ID
     * @return 月度薪资详情
     */
    @GetMapping("/monthly/{id}")
    public Result<SalaryMonthlyVO> getMonthlySalary(@PathVariable Long id) {
        return Result.success(salaryService.getMonthlySalary(id));
    }
}

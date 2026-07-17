package com.hrms.business.salary.controller;

import com.hrms.business.salary.dto.SalaryManagePayslipVerifyRequestDTO;
import com.hrms.business.salary.dto.SalaryManagePayslipQueryDTO;
import com.hrms.business.salary.dto.SalaryPayslipVerifyRequestDTO;
import com.hrms.business.salary.service.SalaryService;
import com.hrms.business.salary.vo.SalaryPayslipDetailVO;
import com.hrms.business.salary.vo.SalaryPayslipListVO;
import com.hrms.business.salary.vo.SalaryPayslipVerifyVO;
import com.hrms.business.salary.vo.SalaryManagePayslipPageVO;
import com.hrms.business.salary.vo.SalaryTrendVO;
import com.hrms.common.web.PageResult;
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

import java.util.List;

/**
 * 工资条控制器。
 */
@RestController
@RequestMapping("/api/v1/salary")
@Tag(name = "工资条", description = "工资条列表、验证、详情和趋势接口")
@RequiredArgsConstructor
public class SalaryPayslipController {

    private final SalaryService salaryService;

    /**
     * 查询当前员工工资条列表。
     *
     * @param month 薪资月份，可为空
     * @return 工资条列表
     * 本方法使用的工具类: Result(hrms-common),List(JDK)
     */
    @GetMapping("/payslips")
    public Result<List<SalaryPayslipListVO>> listPayslips(@RequestParam(required = false) String month) {
        return Result.success(salaryService.listPayslips(month));
    }

    /**
     * 工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/payslip/verify")
    public Result<SalaryPayslipVerifyVO> verifyPayslip(
            @Valid @RequestBody SalaryPayslipVerifyRequestDTO requestDTO) {
        return Result.success(salaryService.verifyPayslip(requestDTO));
    }

    /**
     * 管理端工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/manage/payslip/verify")
    public Result<SalaryPayslipVerifyVO> verifyManagePayslip(
            @Valid @RequestBody SalaryManagePayslipVerifyRequestDTO requestDTO) {
        return Result.success(salaryService.verifyManagePayslip(requestDTO));
    }

    /**
     * 分页查询管理端工资条列表。
     *
     * @param queryDTO 查询参数
     * @return 工资条分页结果
     * 本方法使用的工具类: Result(hrms-common),PageResult(hrms-common)
     */
    @GetMapping("/manage/payslips")
    public Result<PageResult<SalaryManagePayslipPageVO>> pageManagePayslips(
            @Valid SalaryManagePayslipQueryDTO queryDTO) {
        return Result.success(salaryService.pageManagePayslips(queryDTO));
    }

    /**
     * 查询管理端工资条详情。
     *
     * @param id 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/manage/payslip/{id}")
    public Result<SalaryPayslipDetailVO> getManagePayslipDetail(@PathVariable Long id) {
        return Result.success(salaryService.getManagePayslipDetail(id));
    }

    /**
     * 查询工资条详情。
     *
     * @param id 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/payslip/{id}")
    public Result<SalaryPayslipDetailVO> getPayslipDetail(@PathVariable Long id) {
        return Result.success(salaryService.getPayslipDetail(id));
    }

    /**
     * 查询近 6 个月薪资趋势。
     *
     * @return 薪资趋势
     * 本方法使用的工具类: Result(hrms-common),List(JDK)
     */
    @GetMapping("/trend")
    public Result<List<SalaryTrendVO>> getTrend() {
        return Result.success(salaryService.getTrend());
    }
}

package com.hrms.business.personnel.controller;

import com.hrms.business.personnel.dto.RegularApplicationApplyRequestDTO;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.service.RegularApplicationService;
import com.hrms.business.personnel.vo.RegularApplicationApplyVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 转正申请控制器
 */
@RestController
@RequestMapping("/api/v1/regular-applications")
@RequiredArgsConstructor
@Tag(name = "转正申请接口", description = "待转正员工与转正评估接口")
public class RegularApplicationController {

    private final RegularApplicationService regularApplicationService;

    /**
     * 分页查询转正申请列表。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     *
     */
    @GetMapping
    @Operation(summary = "待转正列表")
    public Result<PageResult<RegularApplicationPageVO>> pageRegularApplications(RegularApplicationQueryDTO queryDTO) {
        return Result.success(regularApplicationService.pageRegularApplications(queryDTO));
    }

    /**
     * 发起转正评估。
     *
     * @param employeeId 员工ID
     * @param requestDTO 转正评估请求
     * @return 转正评估发起结果
     *
     */
    @PostMapping("/{employeeId}/apply")
    @Operation(summary = "发起转正")
    public Result<RegularApplicationApplyVO> applyRegular(@PathVariable Long employeeId,
                                                          @Valid @RequestBody RegularApplicationApplyRequestDTO requestDTO) {
        return Result.success(regularApplicationService.applyRegular(employeeId, requestDTO));
    }

}

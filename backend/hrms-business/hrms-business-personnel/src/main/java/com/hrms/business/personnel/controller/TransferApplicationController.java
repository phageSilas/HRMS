package com.hrms.business.personnel.controller;

import com.hrms.business.personnel.dto.TransferApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.TransferApplicationQueryDTO;
import com.hrms.business.personnel.service.TransferApplicationService;
import com.hrms.business.personnel.vo.TransferApplicationCreateVO;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
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
 * 调岗申请控制器
 */
@RestController
@RequestMapping("/api/v1/transfer-applications")
@RequiredArgsConstructor
@Tag(name = "调岗申请接口", description = "调岗申请分页和创建接口")
public class TransferApplicationController {

    private final TransferApplicationService transferApplicationService;

    /**
     * 分页查询调岗申请列表。
     *
     * @param queryDTO 调岗申请查询参数
     * @return 调岗申请分页结果
     *
     */
    @GetMapping
    @Operation(summary = "调岗申请列表")
    public Result<PageResult<TransferApplicationPageVO>> pageTransferApplications(TransferApplicationQueryDTO queryDTO) {
        return Result.success(transferApplicationService.pageTransferApplications(queryDTO));
    }

    /**
     * 创建调岗申请。
     *
     * @param requestDTO 调岗申请创建参数
     * @return 调岗申请创建结果
     *
     */
    @PostMapping
    @Operation(summary = "创建调岗申请")
    public Result<TransferApplicationCreateVO> createTransferApplication(
            @Valid @RequestBody TransferApplicationCreateRequestDTO requestDTO) {
        return Result.success(transferApplicationService.createTransferApplication(requestDTO));
    }

    /**
     * 快速审批通过调岗申请。
     *
     * @param id 调岗申请ID
     * @return 操作结果
     */
    @PostMapping("/{id}/quick-approve")
    @Operation(summary = "快速审批通过调岗申请")
    public Result<Void> quickApproveTransferApplication(@PathVariable Long id) {
        transferApplicationService.quickApproveTransferApplication(id);
        return Result.success();
    }

}

package com.hrms.business.personnel.controller;

import com.hrms.business.personnel.dto.LeaveApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.LeaveApplicationQueryDTO;
import com.hrms.business.personnel.service.LeaveApplicationService;
import com.hrms.business.personnel.vo.LeaveApplicationCreateVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 离职申请控制器
 */
@RestController
@RequestMapping("/api/v1/leave-applications")
@RequiredArgsConstructor
@Tag(name = "离职申请接口", description = "离职申请分页和创建接口")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    /**
     * 分页查询离职申请列表。
     *
     * @param queryDTO 离职申请查询参数
     * @return 离职申请分页结果
     *
     */
    @GetMapping
    @Operation(summary = "离职申请列表")
    public Result<PageResult<LeaveApplicationPageVO>> pageLeaveApplications(LeaveApplicationQueryDTO queryDTO) {
        return Result.success(leaveApplicationService.pageLeaveApplications(queryDTO));
    }

    /**
     * 创建离职申请。
     *
     * @param requestDTO 离职申请创建参数
     * @return 离职申请创建结果
     *
     */
    @PostMapping
    @Operation(summary = "创建离职申请")
    public Result<LeaveApplicationCreateVO> createLeaveApplication(
            @Valid @RequestBody LeaveApplicationCreateRequestDTO requestDTO) {
        return Result.success(leaveApplicationService.createLeaveApplication(requestDTO));
    }

}

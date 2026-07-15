package com.hrms.business.mycenter.controller;

import com.hrms.business.mycenter.dto.LeaveBalanceVO;
import com.hrms.business.mycenter.dto.LeaveRequestDTO;
import com.hrms.business.mycenter.dto.LeaveVO;
import com.hrms.business.mycenter.service.LeaveService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
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

import java.util.List;

/**
 * 请假控制器
 * API-MYC-07 ~ 10
 */
@RestController("myCenterLeaveController")
@RequestMapping("/api/v1/leave")
@Tag(name = "个人请假", description = "请假申请、记录查询、取消")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final UserMapper userMapper;

    @PostMapping
    @Operation(summary = "提交请假申请", description = "创建请假申请")
    public Result<Void> createLeave(@Valid @RequestBody LeaveRequestDTO request) {
        Long employeeId = getEmployeeId();
        leaveService.createLeave(employeeId, request);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "查询请假记录", description = "请假记录列表")
    public Result<List<LeaveVO>> listLeaves() {
        Long employeeId = getEmployeeId();
        return Result.success(leaveService.listLeaves(employeeId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消请假", description = "仅草稿或审批中的请假可取消")
    public Result<Void> cancelLeave(@PathVariable("id") Long leaveId) {
        Long employeeId = getEmployeeId();
        leaveService.cancelLeave(employeeId, leaveId);
        return Result.success();
    }

    @GetMapping("/balance")
    @Operation(summary = "查询假期余额", description = "年假/调休余额")
    public Result<LeaveBalanceVO> getLeaveBalance() {
        Long employeeId = getEmployeeId();
        return Result.success(leaveService.getLeaveBalance(employeeId));
    }

    /**
     * 从当前登录用户获取员工ID
     */
    private Long getEmployeeId() {
        Long userId = SecurityContextHolder.getUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getEmployeeId() == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户未关联员工信息");
        }
        return user.getEmployeeId();
    }
}

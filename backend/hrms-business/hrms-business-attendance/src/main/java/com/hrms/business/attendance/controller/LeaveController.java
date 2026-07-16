package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.dto.LeaveCreateRequestDTO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 请假管理控制器。
 */
@RestController
@RequestMapping("/api/v1/leaves")
@Tag(name = "请假管理", description = "请假类型、余额与申请接口")
@RequiredArgsConstructor
public class LeaveController {

    private final AttendanceService attendanceService;

    /**
     * 查询启用的请假类型。
     *
     * @return 请假类型列表
     * 本方法使用的工具类: Result(hrms-common),List(JDK)
     */
    @GetMapping("/types")
    public Result<List<LeaveTypeVO>> listLeaveTypes() {
        return Result.success(attendanceService.listLeaveTypes());
    }

    /**
     * 查询当前员工假期余额。
     *
     * @return 假期余额列表
     * 本方法使用的工具类: Result(hrms-common),List(JDK)
     */
    @GetMapping("/balances")
    public Result<List<LeaveBalanceVO>> listLeaveBalances() {
        return Result.success(attendanceService.listLeaveBalances());
    }

    /**
     * 提交请假申请。
     *
     * @param requestDTO 请假申请请求
     * @return 创建结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping
    public Result<LeaveCreateVO> createLeave(@Valid @RequestBody LeaveCreateRequestDTO requestDTO) {
        return Result.success(attendanceService.createLeave(requestDTO));
    }
}

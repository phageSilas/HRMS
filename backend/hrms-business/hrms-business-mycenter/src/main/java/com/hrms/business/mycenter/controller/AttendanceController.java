package com.hrms.business.mycenter.controller;

import com.hrms.business.mycenter.dto.AttendanceCalendarVO;
import com.hrms.business.mycenter.dto.AttendanceStatisticsVO;
import com.hrms.business.mycenter.dto.ClockInRequest;
import com.hrms.business.mycenter.dto.MakeupRecordVO;
import com.hrms.business.mycenter.dto.MakeupRequest;
import com.hrms.business.mycenter.dto.OvertimeRecordVO;
import com.hrms.business.mycenter.dto.OvertimeRequest;
import com.hrms.business.mycenter.service.AttendanceService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人考勤控制器
 * API-MYC-03 ~ 06, 考勤统计, 加班申请
 */
@RestController("myCenterAttendanceController")
@RequestMapping("/api/v1/attendance")
@Tag(name = "个人考勤", description = "考勤日历、打卡、补卡申请")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserMapper userMapper;

    @GetMapping("/calendar")
    @Operation(summary = "获取考勤日历", description = "个人月度考勤日历")
    public Result<AttendanceCalendarVO> getCalendar(@RequestParam("yearMonth") String yearMonth) {
        Long employeeId = getEmployeeId();
        return Result.success(attendanceService.getCalendar(employeeId, yearMonth));
    }

    @PostMapping("/clock-in")
    @Operation(summary = "打卡", description = "网页端打卡（上班/下班）")
    public Result<Void> clockIn(@Valid @RequestBody ClockInRequest request) {
        Long employeeId = getEmployeeId();
        attendanceService.clockIn(employeeId, request.getType());
        return Result.success();
    }

    @PostMapping("/makeup")
    @Operation(summary = "申请补卡", description = "提交补卡申请")
    public Result<Void> createMakeup(@Valid @RequestBody MakeupRequest request) {
        Long employeeId = getEmployeeId();
        attendanceService.createMakeup(employeeId, request);
        return Result.success();
    }

    @GetMapping("/makeup/list")
    @Operation(summary = "补卡记录", description = "补卡申请列表")
    public Result<List<MakeupRecordVO>> listMakeupRecords() {
        Long employeeId = getEmployeeId();
        return Result.success(attendanceService.listMakeupRecords(employeeId));
    }

    @PostMapping("/overtime")
    @Operation(summary = "提交加班申请", description = "提交加班申请")
    public Result<Void> createOvertime(@Valid @RequestBody OvertimeRequest request) {
        Long employeeId = getEmployeeId();
        attendanceService.createOvertime(employeeId, request);
        return Result.success();
    }

    @GetMapping("/overtime")
    @Operation(summary = "加班记录列表", description = "查询本人加班申请记录")
    public Result<List<OvertimeRecordVO>> listOvertimeRecords() {
        Long employeeId = getEmployeeId();
        return Result.success(attendanceService.listOvertimeRecords(employeeId));
    }

    @GetMapping("/statistics")
    @Operation(summary = "考勤统计", description = "个人月度考勤统计数据")
    public Result<AttendanceStatisticsVO> getStatistics(@RequestParam("yearMonth") String yearMonth) {
        Long employeeId = getEmployeeId();
        return Result.success(attendanceService.getStatistics(employeeId, yearMonth));
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

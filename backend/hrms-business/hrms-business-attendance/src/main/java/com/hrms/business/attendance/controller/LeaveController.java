package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}

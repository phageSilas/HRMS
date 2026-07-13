package com.hrms.business.attendance.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 考勤管理控制器
 */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "考勤管理", description = "考勤记录、请假申请、补卡申请等接口")
public class AttendanceController {

    /**
     * 获取考勤记录列表
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

}

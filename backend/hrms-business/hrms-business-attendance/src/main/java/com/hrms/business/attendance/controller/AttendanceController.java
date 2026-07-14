package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤管理控制器。
 */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "考勤管理", description = "考勤组、打卡、补卡、请假与统计接口")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 分页查询考勤组。
     *
     * @param queryDTO 查询参数
     * @return 考勤组分页结果
     * 本方法使用的工具类: Result(hrms-common),PageResult(hrms-common)
     */
    @GetMapping("/groups")
    public Result<PageResult<AttendanceGroupPageVO>> pageAttendanceGroups(@Valid AttendanceGroupQueryDTO queryDTO) {
        return Result.success(attendanceService.pageAttendanceGroups(queryDTO));
    }

    /**
     * 获取考勤记录占位接口。
     *
     * @return 空结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }
}

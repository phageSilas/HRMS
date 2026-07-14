package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

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
     * 创建考勤组。
     *
     * @param requestDTO 创建请求
     * @return 创建后的考勤组
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/groups")
    public Result<AttendanceGroupPageVO> createAttendanceGroup(
            @Valid @RequestBody AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(attendanceService.createAttendanceGroup(requestDTO));
    }

    /**
     * 更新考勤组。
     *
     * @param id         考勤组ID
     * @param requestDTO 更新请求
     * @return 更新后的考勤组
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PutMapping("/groups/{id}")
    public Result<AttendanceGroupPageVO> updateAttendanceGroup(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(attendanceService.updateAttendanceGroup(id, requestDTO));
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

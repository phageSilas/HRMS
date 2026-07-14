package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 当前登录员工打卡。
     *
     * @param requestDTO     打卡请求
     * @param servletRequest HTTP 请求
     * @return 打卡结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/clock")
    public Result<AttendanceClockVO> clock(@RequestBody AttendanceClockRequestDTO requestDTO,
                                           HttpServletRequest servletRequest) {
        return Result.success(attendanceService.clock(requestDTO, resolveClientIp(servletRequest)));
    }

    /**
     * 查询当前员工个人月度打卡日历。
     *
     * @param yearMonth 月份，格式 yyyy-MM
     * @return 个人月度打卡日历
     * 本方法使用的工具类: Result(hrms-common)
     */
    @GetMapping("/records/my-calendar")
    public Result<AttendanceCalendarVO> getMyCalendar(@RequestParam String yearMonth) {
        return Result.success(attendanceService.getMyCalendar(yearMonth));
    }

    /**
     * 创建补卡申请。
     *
     * @param requestDTO 补卡申请请求
     * @return 补卡申请创建结果
     * 本方法使用的工具类: Result(hrms-common)
     */
    @PostMapping("/corrections")
    public Result<AttendanceCorrectionCreateVO> createCorrection(
            @Valid @RequestBody AttendanceCorrectionCreateRequestDTO requestDTO) {
        return Result.success(attendanceService.createCorrection(requestDTO));
    }

    /**
     * 解析客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     * 本方法使用的工具类: 无
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
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

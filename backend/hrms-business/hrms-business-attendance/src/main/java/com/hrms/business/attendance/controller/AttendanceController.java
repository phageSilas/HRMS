package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.dto.AttendanceCalendarConfigRequestDTO;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceLeaveManageQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupRecordQueryDTO;
import com.hrms.business.attendance.dto.MonthlyStatGenerateRequestDTO;
import com.hrms.business.attendance.service.AttendanceCalendarConfigService;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceCalendarConfigVO;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.MonthlyStatGenerateVO;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.business.attendance.vo.AttendanceGroupRecordPageVO;
import com.hrms.business.attendance.vo.AttendanceLeaveManageItemVO;
import com.hrms.business.attendance.vo.AttendanceSummaryDashboardVO;
import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

/**
 * 考勤管理控制器。
 */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "考勤管理", description = "考勤组、打卡、补卡、请假与统计接口")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceCalendarConfigService attendanceCalendarConfigService;

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
     * 获取考勤日历配置。
     *
     * @param year 年份
     * @return 考勤日历配置
     */
    @GetMapping("/calendar-config")
    public Result<AttendanceCalendarConfigVO> getCalendarConfig(@RequestParam Integer year) {
        return Result.success(attendanceCalendarConfigService.getCalendarConfig(year));
    }

    /**
     * 保存考勤日历配置。
     *
     * @param requestDTO 请求参数
     * @return 保存后的考勤日历配置
     */
    @PutMapping("/calendar-config")
    public Result<AttendanceCalendarConfigVO> saveCalendarConfig(
            @Valid @RequestBody AttendanceCalendarConfigRequestDTO requestDTO) {
        return Result.success(attendanceCalendarConfigService.saveCalendarConfig(requestDTO));
    }

    /**
     * 创建考勤组。
     *
     * @param requestDTO 创建请求
     * @return 创建后的考勤组
     *
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
     *
     */
    @PutMapping("/groups/{id}")
    public Result<AttendanceGroupPageVO> updateAttendanceGroup(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(attendanceService.updateAttendanceGroup(id, requestDTO));
    }

    /**
     * 逻辑删除考勤组。
     *
     * @param id 考勤组ID
     * @return 删除结果
     *
     */
    @DeleteMapping("/groups/{id}")
    public Result<Void> deleteAttendanceGroup(@PathVariable Long id) {
        attendanceService.deleteAttendanceGroup(id);
        return Result.success();
    }

    /**
     * 分页查询考勤组打卡记录。
     *
     * @param groupId  考勤组ID
     * @param queryDTO 查询参数
     * @return 考勤组打卡记录分页结果
     *
     */
    @GetMapping("/groups/{groupId}/records")
    public Result<PageResult<AttendanceGroupRecordPageVO>> pageGroupAttendanceRecords(
            @PathVariable Long groupId,
            @Valid AttendanceGroupRecordQueryDTO queryDTO) {
        return Result.success(attendanceService.pageGroupAttendanceRecords(groupId, queryDTO));
    }

    /**
     * 当前登录员工打卡。
     *
     * @param requestDTO     打卡请求
     * @param servletRequest HTTP 请求
     * @return 打卡结果
     *
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
     *
     */
    @GetMapping("/records/my-calendar")
    public Result<AttendanceCalendarVO> getMyCalendar(@RequestParam String yearMonth) {
        return Result.success(attendanceService.getMyCalendar(yearMonth));
    }

    /**
     * 查询HR和主管考勤统计看板。
     *
     * @param yearMonth 月份，格式yyyy-MM
     * @param deptId    部门ID
     * @return 考勤统计看板
     *
     */
    @GetMapping("/summary/dashboard")
    public Result<AttendanceSummaryDashboardVO> getSummaryDashboard(@RequestParam String yearMonth,
                                                                    @RequestParam(required = false) Long deptId,
                                                                    @RequestParam(required = false) Boolean refreshCache) {
        return Result.success(attendanceService.getSummaryDashboard(yearMonth, deptId, refreshCache));
    }

    /**
     * 分页查询管理侧请假记录。
     *
     * @param queryDTO 查询参数
     * @return 请假管理列表
     *
     */
    @GetMapping("/leaves")
    public Result<PageResult<AttendanceLeaveManageItemVO>> pageLeaveManageList(
            @Valid AttendanceLeaveManageQueryDTO queryDTO) {
        return Result.success(attendanceService.pageLeaveManageList(queryDTO));
    }

    /**
     * 快速审批通过请假申请。
     *
     * @param id 请假申请 ID
     * @return 处理结果
     */
    @PostMapping("/leaves/{id}/quick-approve")
    public Result<Void> quickApproveLeaveRequest(@PathVariable Long id) {
        attendanceService.quickApproveLeaveRequest(id);
        return Result.success();
    }

    /**
     * 创建补卡申请。
     *
     * @param requestDTO 补卡申请请求
     * @return 补卡申请创建结果
     *
     */
    @PostMapping("/corrections")
    public Result<AttendanceCorrectionCreateVO> createCorrection(
            @Valid @RequestBody AttendanceCorrectionCreateRequestDTO requestDTO) {
        return Result.success(attendanceService.createCorrection(requestDTO));
    }

    /**
     * 生成月度考勤统计。
     *
     * @param requestDTO 生成请求
     * @return 生成结果
     *
     */
    @PostMapping("/stats/monthly/generate")
    public Result<MonthlyStatGenerateVO> generateMonthlyStat(
            @Valid @RequestBody MonthlyStatGenerateRequestDTO requestDTO) {
        return Result.success(attendanceService.generateMonthlyStat(requestDTO));
    }

    /**
     * 查询薪资模块月度考勤数据源。
     *
     * @param month       月份
     * @param employeeIds 员工ID逗号分隔
     * @return 薪资考勤数据源
     *
     */
    @GetMapping("/stats/monthly/payroll-source")
    public Result<List<AttendancePayrollSourceVO>> getPayrollSource(@RequestParam String month,
                                                                    @RequestParam(required = false) String employeeIds) {
        return Result.success(attendanceService.getPayrollSource(month, parseEmployeeIds(employeeIds)));
    }

    /**
     * 解析员工ID参数。
     *
     * @param employeeIds 员工ID逗号分隔
     * @return 员工ID列表
     * 本方法使用的工具类: Arrays(JDK),List(JDK)
     */
    private List<Long> parseEmployeeIds(String employeeIds) {
        if (employeeIds == null || employeeIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(employeeIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    /**
     * 解析客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     * 本方法使用的工具类: HttpServletRequest(jakarta.servlet)
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Real-IP"
        };
        for (String headerName : headerNames) {
            String ip = firstValidIp(request.getHeader(headerName));
            if (ip != null) {
                return ip;
            }
        }
        return firstValidIp(request.getRemoteAddr());
    }

    /**
     * 从代理 IP 列表中获取第一个有效 IP。
     *
     * @param ipValue IP 或逗号分隔的 IP 列表
     * @return 第一个有效 IP
     * 本方法使用的工具类: 无
     */
    private String firstValidIp(String ipValue) {
        if (ipValue == null || ipValue.isBlank()) {
            return null;
        }
        for (String ip : ipValue.split(",")) {
            String trimmedIp = ip.trim();
            if (!trimmedIp.isBlank() && !"unknown".equalsIgnoreCase(trimmedIp)) {
                return trimmedIp;
            }
        }
        return null;
    }


}

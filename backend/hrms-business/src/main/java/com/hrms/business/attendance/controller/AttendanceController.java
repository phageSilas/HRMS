package com.hrms.business.attendance.controller;

import com.hrms.business.attendance.dto.AttendanceRecordQueryDTO;
import com.hrms.business.attendance.dto.LeaveApplyRequestDTO;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceRecordVO;
import com.hrms.business.attendance.vo.LeaveApplyVO;
import com.hrms.common.model.PageResult;
import com.hrms.common.model.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供考勤管理 HTTP 接口。
 */
@RestController
@RequestMapping("/api/business/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 创建考勤控制器。
     *
     * @param attendanceService 考勤业务服务
     */
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 分页查询考勤记录。
     *
     * @param queryParam 考勤记录查询参数
     * @return 考勤记录分页结果
     */
    @PostMapping("/records/page")
    public Result<PageResult<AttendanceRecordVO>> pageRecords(@Valid @RequestBody AttendanceRecordQueryDTO queryParam) {
        return Result.success(attendanceService.pageRecords(queryParam));
    }

    /**
     * 提交请假申请。
     *
     * @param requestParam 请假申请请求参数
     * @return 请假申请结果
     */
    @PostMapping("/leave")
    public Result<LeaveApplyVO> applyLeave(@Valid @RequestBody LeaveApplyRequestDTO requestParam) {
        return Result.success(attendanceService.applyLeave(requestParam));
    }
}

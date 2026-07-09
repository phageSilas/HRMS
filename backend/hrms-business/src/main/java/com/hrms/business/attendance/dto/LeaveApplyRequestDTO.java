package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 请假申请请求参数。
 *
 * @param employeeId 员工ID
 * @param leaveType 请假类型
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param reason 请假原因
 */
public record LeaveApplyRequestDTO(
    @NotNull(message = "不能为空")
    Long employeeId,
    String leaveType,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String reason
) {
}

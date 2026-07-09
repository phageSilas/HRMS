package com.hrms.business.process.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 员工流程发起请求参数。
 *
 * @param employeeId 员工ID
 * @param effectiveDate 生效日期
 * @param reason 流程原因
 */
public record EmployeeProcessStartRequestDTO(
    @NotNull(message = "不能为空")
    Long employeeId,
    LocalDate effectiveDate,
    String reason
) {
}

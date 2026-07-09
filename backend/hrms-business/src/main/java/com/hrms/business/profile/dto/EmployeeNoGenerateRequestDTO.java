package com.hrms.business.profile.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 员工工号生成请求参数。
 *
 * @param departmentId 所属部门ID
 */
public record EmployeeNoGenerateRequestDTO(
    @NotNull(message = "不能为空")
    Long departmentId
) {
}

package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门合并 DTO
 */
@Data
@Schema(description = "部门合并请求体")
public class DeptMergeDTO {

    @NotNull(message = "目标部门ID不能为空")
    @Schema(description = "目标部门ID（接收员工的部门）", example = "10", required = true)
    private Long targetDeptId;

}

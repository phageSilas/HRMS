package com.hrms.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单分配 DTO
 */
@Data
@Schema(description = "角色菜单分配请求体")
public class RoleMenuAssignDTO {

    @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3, 10, 11, 20]")
    private List<Long> menuIds;

}

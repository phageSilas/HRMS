package com.hrms.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色更新 DTO
 */
@Data
@Schema(description = "角色更新请求体")
public class RoleUpdateDTO {

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色编码", example = "ADMIN")
    private String roleCode;

    @Schema(description = "数据权限范围：1-仅本人 2-本部门 3-本部门及子部门 4-全部", example = "1")
    private Integer dataScope;

    @Schema(description = "排序号", example = "0")
    private Integer sortNo;

    @Schema(description = "状态：1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "系统管理员角色")
    private String remark;

}

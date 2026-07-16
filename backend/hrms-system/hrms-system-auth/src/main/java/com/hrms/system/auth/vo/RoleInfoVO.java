package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色信息 VO（用于登录和当前用户信息接口）
 */
@Data
@Schema(description = "角色信息")
public class RoleInfoVO {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "数据权限范围：1-仅本人 2-本部门 3-本部门及子部门 4-全部")
    private Integer dataScope;

}

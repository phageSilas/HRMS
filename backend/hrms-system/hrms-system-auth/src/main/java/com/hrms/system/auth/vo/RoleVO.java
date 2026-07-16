package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色 VO
 */
@Data
@Schema(description = "角色响应对象")
public class RoleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "数据权限范围：1-仅本人 2-本部门 3-本部门及子部门 4-全部")
    private Integer dataScope;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;

    @Schema(description = "备注")
    private String remark;

}

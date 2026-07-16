package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 当前用户信息 VO
 */
@Data
@Schema(description = "当前用户信息")
public class CurrentUserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

    @Schema(description = "角色列表")
    private List<RoleInfoVO> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "菜单树")
    private List<MenuVO> menus;

    @Schema(description = "关联员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

}

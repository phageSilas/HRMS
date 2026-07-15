package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 登录用户信息 VO
 */
@Data
@Schema(description = "登录用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色列表")
    private List<RoleInfoVO> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;

}

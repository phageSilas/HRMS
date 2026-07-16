package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表 VO
 */
@Data
@Schema(description = "用户列表响应对象")
public class UserListVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "关联员工ID")
    private Long employeeId;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

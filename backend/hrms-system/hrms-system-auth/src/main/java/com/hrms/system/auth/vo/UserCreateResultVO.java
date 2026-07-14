package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建用户响应 VO
 */
@Data
@Schema(description = "创建用户响应对象")
public class UserCreateResultVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "是否需要修改密码")
    private Boolean needChangePassword;

}

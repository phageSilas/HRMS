package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 重置密码响应 VO
 */
@Data
@Schema(description = "重置密码响应对象")
public class ResetPasswordVO {

    @Schema(description = "新密码（明文，仅返回一次）")
    private String newPassword;

    @Schema(description = "是否需要修改密码")
    private Boolean needChangePassword;

}

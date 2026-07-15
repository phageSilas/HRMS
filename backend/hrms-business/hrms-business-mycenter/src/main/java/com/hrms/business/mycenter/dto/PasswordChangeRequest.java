package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求 DTO
 */
@Data
@Schema(description = "修改密码请求")
public class PasswordChangeRequest {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码（8位以上，大小写+数字+特殊字符至少3种）")
    private String newPassword;
}

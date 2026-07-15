package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定手机请求 DTO
 */
@Data
@Schema(description = "绑定手机请求")
public class PhoneBindRequest {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "新手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String verifyCode;
}

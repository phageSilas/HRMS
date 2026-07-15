package com.hrms.business.mycenter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志 VO
 */
@Data
@Schema(description = "登录日志")
public class LoginLogVO {

    @Schema(description = "登录IP")
    private String ip;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "状态：1-成功 0-失败")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}

package com.hrms.business.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改会话标题请求 DTO
 * <p>
 * 用于 AI 对话历史列表中的会话重命名。
 * 新标题会覆盖原有默认标题（自动生成或用户第一次消息截取）。
 * </p>
 *
 * @since 2026-07-20
 */
@Data
public class UpdateTitleRequestDTO {

    /**
     * 新标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

}

package com.hrms.business.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改会话标题请求 DTO
 */
@Data
public class UpdateTitleRequestDTO {

    /**
     * 新标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

}

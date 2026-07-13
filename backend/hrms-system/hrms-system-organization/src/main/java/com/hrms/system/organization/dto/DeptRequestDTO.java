package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 部门请求 DTO
 */
@Data
@Schema(description = "部门请求")
public class DeptRequestDTO {

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "部门编码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

}

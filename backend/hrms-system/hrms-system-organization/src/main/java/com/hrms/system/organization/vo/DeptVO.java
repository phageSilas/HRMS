package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门 VO
 */
@Data
@Schema(description = "部门")
public class DeptVO {

    @Schema(description = "部门ID")
    private Long id;

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

    @Schema(description = "子部门列表")
    private List<DeptVO> children;

}

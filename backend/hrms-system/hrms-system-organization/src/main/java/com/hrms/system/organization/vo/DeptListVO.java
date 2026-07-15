package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门平铺列表 VO
 */
@Data
@Schema(description = "部门平铺列表")
public class DeptListVO {

    @Schema(description = "部门 ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "上级部门 ID")
    private Long parentId;

    @Schema(description = "部门层级")
    private Integer deptLevel;

}

package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门详情 VO
 */
@Data
@Schema(description = "部门详情")
public class DeptDetailVO {

    @Schema(description = "部门 ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "上级部门 ID")
    private Long parentId;

    @Schema(description = "上级部门名称")
    private String parentName;

    @Schema(description = "祖级路径")
    private String ancestors;

    @Schema(description = "部门层级")
    private Integer deptLevel;

    @Schema(description = "部门负责人用户 ID")
    private Long leaderUserId;

    @Schema(description = "部门负责人员工 ID")
    private Long leaderEmployeeId;

    @Schema(description = "员工数量")
    private Integer employeeCount;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

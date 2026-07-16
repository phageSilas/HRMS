package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门树节点 VO
 */
@Data
@Schema(description = "部门树节点")
public class DeptTreeVO {

    @Schema(description = "部门 ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "上级部门 ID")
    private Long parentId;

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

    @Schema(description = "子部门列表")
    private List<DeptTreeVO> children;

}

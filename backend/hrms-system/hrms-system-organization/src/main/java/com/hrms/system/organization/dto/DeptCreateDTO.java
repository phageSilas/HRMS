package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建部门 DTO
 */
@Data
@Schema(description = "创建部门请求体")
public class DeptCreateDTO {

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    private String deptName;

    @Schema(description = "部门编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "RD001")
    private String deptCode;

    @Schema(description = "上级部门 ID，不传或传 0 表示根部门", example = "0")
    private Long parentId;

    @Schema(description = "部门负责人用户 ID", example = "1")
    private Long leaderUserId;

    @Schema(description = "排序号", example = "1")
    private Integer sortNo;

    @Schema(description = "备注", example = "负责产品研发")
    private String remark;

}

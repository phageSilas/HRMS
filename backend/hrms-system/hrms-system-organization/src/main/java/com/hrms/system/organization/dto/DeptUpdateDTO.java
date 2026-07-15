package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新部门 DTO
 */
@Data
@Schema(description = "更新部门请求体")
public class DeptUpdateDTO {

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "部门负责人用户 ID", example = "1")
    private Long leaderUserId;

    @Schema(description = "排序号", example = "1")
    private Integer sortNo;

    @Schema(description = "备注", example = "负责产品研发")
    private String remark;

}

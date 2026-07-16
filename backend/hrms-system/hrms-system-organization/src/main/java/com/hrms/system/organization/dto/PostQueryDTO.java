package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 职位查询 DTO
 */
@Data
@Schema(description = "职位查询参数")
public class PostQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "部门 ID", example = "1")
    private Long deptId;

    @Schema(description = "序列编码：M/P/S", example = "P")
    private String sequenceCode;

    @Schema(description = "关键词（职位名称或编码）", example = "Java")
    private String keyword;

}

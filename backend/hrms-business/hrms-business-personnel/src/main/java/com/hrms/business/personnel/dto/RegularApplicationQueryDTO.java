package com.hrms.business.personnel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转正申请分页查询 DTO
 */
@Data
@Schema(description = "转正申请分页查询")
public class RegularApplicationQueryDTO {

    /**
     * 列表页签：pending-待转正，evaluated-已评估
     */
    @Schema(description = "列表页签：pending-待转正，evaluated-已评估")
    private String tab = "pending";

    /**
     * 关键词，匹配员工姓名或工号
     */
    @Schema(description = "关键词，匹配员工姓名或工号")
    private String keyword;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;

}

package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 调岗申请分页查询 DTO
 */
@Data
@Schema(description = "调岗申请分页查询")
public class TransferApplicationQueryDTO {

    /**
     * 关键词，匹配员工姓名或工号
     */
    @Schema(description = "关键词，匹配员工姓名或工号")
    private String keyword;

    /**
     * 原部门ID
     */
    @Schema(description = "原部门ID")
    private Long departmentId;

    /**
     * 审批状态
     */
    @JsonAlias("status")
    @Schema(description = "审批状态")
    private Integer approvalStatus;

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

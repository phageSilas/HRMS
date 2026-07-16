package com.hrms.business.personnel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 入职申请分页查询 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职申请分页查询")
public class EntryApplicationQueryDTO {

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "20")
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * 关键词，匹配候选人姓名或手机号
     */
    @Schema(description = "关键词，匹配候选人姓名或手机号")
    private String keyword;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态：0-草稿，1-审批中，2-已通过，3-已拒绝，5-已入职")
    private Integer approvalStatus;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 申请日期起始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "申请日期起始")
    private LocalDate dateStart;

    /**
     * 申请日期结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "申请日期结束")
    private LocalDate dateEnd;

}

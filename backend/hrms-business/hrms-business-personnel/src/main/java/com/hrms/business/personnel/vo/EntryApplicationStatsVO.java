package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入职申请状态统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职申请状态统计")
public class EntryApplicationStatsVO {

    /**
     * 全部数量
     */
    @Schema(description = "全部数量")
    private Long all;

    /**
     * 草稿数量
     */
    @Schema(description = "草稿数量")
    private Long draft;

    /**
     * 审批中数量
     */
    @Schema(description = "审批中数量")
    private Long approving;

    /**
     * 已批准待入职数量
     */
    @Schema(description = "已批准待入职数量")
    private Long approved;

    /**
     * 已拒绝数量
     */
    @Schema(description = "已拒绝数量")
    private Long rejected;

    /**
     * 已入职数量
     */
    @Schema(description = "已入职数量")
    private Long entered;
}

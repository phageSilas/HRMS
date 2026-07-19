package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待办列表查询参数
 */
@Data
@Schema(description = "待办列表查询参数")
public class PendingTaskQuery {

    @Schema(description = "业务类型筛选")
    private String businessType;

    @Schema(description = "关键词（标题搜索）")
    private String keyword;

    @Schema(description = "开始时间")
    private String startDate;

    @Schema(description = "结束时间")
    private String endDate;

    @Schema(description = "筛选类型: pending-待审批(默认), today-approved-今日已审批, overdue-已逾期")
    private String filterType;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;
}

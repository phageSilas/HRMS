package com.hrms.business.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 薪资批次明细分页查询 DTO。
 */
@Data
@Schema(description = "薪资批次明细分页查询参数")
public class SalaryBatchItemQueryDTO {

    @Schema(description = "每页条数，最大100", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "游标分页：上一页最后一条记录的ID（首次不传）", example = "5001")
    private Long lastId;

    @Schema(description = "页码（用于缓存key，游标模式下忽略）", example = "1")
    private Integer pageNum = 1;
}

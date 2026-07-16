package com.hrms.business.mycenter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班记录 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "加班记录")
public class OvertimeRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "加班日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime overtimeDate;

    @Schema(description = "加班时长（小时）")
    private BigDecimal duration;

    @Schema(description = "加班事由")
    private String reason;

    @Schema(description = "审批状态：0-草稿 1-审批中 2-已通过 3-已驳回")
    private Integer approvalStatus;

    @Schema(description = "审批状态描述")
    private String approvalStatusDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

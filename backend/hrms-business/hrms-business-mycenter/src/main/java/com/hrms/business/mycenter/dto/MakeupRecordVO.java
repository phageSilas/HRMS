package com.hrms.business.mycenter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 补卡记录 VO
 */
@Data
@Schema(description = "补卡记录")
public class MakeupRecordVO {

    @Schema(description = "补卡ID")
    private Long id;

    @Schema(description = "补卡日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate correctionDate;

    @Schema(description = "补卡类型：CLOCK_IN-上班补卡 CLOCK_OUT-下班补卡")
    private String correctionType;

    @Schema(description = "补卡原因")
    private String correctionReason;

    @Schema(description = "审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝")
    private Integer approvalStatus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

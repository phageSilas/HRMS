package com.hrms.business.mycenter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假记录 VO
 */
@Data
@Schema(description = "请假记录")
public class LeaveVO {

    @Schema(description = "请假ID")
    private Long id;

    @Schema(description = "请假类型：ANNUAL-年假 COMPASSIONATE-调休 SICK-病假 PERSONAL-事假")
    private String leaveType;

    @Schema(description = "请假类型描述")
    private String leaveTypeDesc;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "请假天数")
    private BigDecimal totalDays;

    @Schema(description = "请假原因")
    private String leaveReason;

    @Schema(description = "审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝 4-已撤回")
    private Integer approvalStatus;

    @Schema(description = "审批状态描述")
    private String approvalStatusDesc;

    @Schema(description = "审批实例ID（用于查看审批进度）")
    private Long approvalInstanceId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

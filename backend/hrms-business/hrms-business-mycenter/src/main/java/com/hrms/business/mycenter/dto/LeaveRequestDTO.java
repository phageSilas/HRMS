package com.hrms.business.mycenter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请请求 DTO
 */
@Data
@Schema(description = "请假申请请求")
public class LeaveRequestDTO {

    @NotBlank(message = "请假类型不能为空")
    @Schema(description = "请假类型：ANNUAL-年假 COMPASSIONATE-调休 SICK-病假 PERSONAL-事假")
    private String leaveType;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @NotNull(message = "请假天数不能为空")
    @Schema(description = "请假天数")
    private BigDecimal totalDays;

    @Schema(description = "请假小时数（按小时请假时使用）")
    private BigDecimal totalHours;

    @Schema(description = "请假原因")
    private String leaveReason;

    @Schema(description = "附件地址")
    private String attachmentUrl;
}

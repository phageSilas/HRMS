package com.hrms.business.mycenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 假期余额 VO
 */
@Data
@Schema(description = "假期余额")
public class LeaveBalanceVO {

    @Schema(description = "年假总天数")
    private BigDecimal annualTotal;

    @Schema(description = "年假已用天数")
    private BigDecimal annualUsed;

    @Schema(description = "年假剩余天数")
    private BigDecimal annualRemaining;

    @Schema(description = "病假总天数")
    private BigDecimal sickTotal;

    @Schema(description = "病假已用天数")
    private BigDecimal sickUsed;

    @Schema(description = "病假剩余天数")
    private BigDecimal sickRemaining;

    @Schema(description = "调休总天数")
    private BigDecimal compassionateTotal;

    @Schema(description = "调休已用天数")
    private BigDecimal compassionateUsed;

    @Schema(description = "调休剩余天数")
    private BigDecimal compassionateRemaining;
}

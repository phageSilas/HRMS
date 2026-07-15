package com.hrms.business.attendance.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 假期余额视图。
 */
@Data
public class LeaveBalanceVO {

    /**
     * 请假类型。
     */
    private String leaveType;

    /**
     * 类型名称。
     */
    private String leaveTypeName;

    /**
     * 总额度。
     */
    private BigDecimal totalDays;

    /**
     * 已使用天数。
     */
    private BigDecimal usedDays;

    /**
     * 剩余天数。
     */
    private BigDecimal remainingDays;
}

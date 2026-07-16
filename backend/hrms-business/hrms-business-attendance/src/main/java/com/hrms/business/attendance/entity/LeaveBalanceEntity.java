package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 假期余额实体，对应 hr_leave_balance。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_leave_balance")
public class LeaveBalanceEntity extends BaseEntity {

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 假期类型。
     */
    private String leaveType;

    /**
     * 余额所属年份。
     */
    private Integer balanceYear;

    /**
     * 本年度应得天数。
     */
    private BigDecimal totalDays;

    /**
     * 已使用天数。
     */
    private BigDecimal usedDays;

    /**
     * 审批中冻结天数。
     */
    private BigDecimal frozenDays;

    /**
     * 剩余可用天数。
     */
    private BigDecimal remainingDays;

    /**
     * 上年结转天数。
     */
    private BigDecimal carryoverDays;

    /**
     * 人工调整天数。
     */
    private BigDecimal adjustDays;

    /**
     * 余额过期日期。
     */
    private LocalDate expireDate;

    /**
     * 状态：0-停用 1-启用。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

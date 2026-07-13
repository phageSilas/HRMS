package com.hrms.business.salary.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资批次实体
 */
@Data
public class SalaryBatchEntity extends BaseEntity {

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 员工数量
     */
    private Integer employeeCount;

    /**
     * 应发总额
     */
    private BigDecimal totalPayable;

    /**
     * 实发总额
     */
    private BigDecimal totalActual;

    /**
     * 状态：0-草稿，1-计算中，2-待确认，3-审批中，4-已通过，5-已发放
     */
    private Integer status;

    /**
     * 审批状态
     */
    private Integer approvalStatus;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

}

package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资核算明细实体，对齐 hr_salary_batch_item 表；本表没有审计人、逻辑删除和 version 字段。
 */
@Data
@TableName("hr_salary_batch_item")
public class SalaryBatchItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;

    private Long employeeId;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal performanceBonus;

    private BigDecimal overtimePay;

    private BigDecimal lateDeduction;

    private BigDecimal leaveDeduction;

    private BigDecimal socialInsurance;

    private BigDecimal housingFund;

    private BigDecimal incomeTax;

    private BigDecimal grossSalary;

    private BigDecimal deductionTotal;

    private BigDecimal netSalary;

    private String warningLevel;

    private String warningReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资批次人工调整实体，对齐 hr_salary_batch_adjustment 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hr_salary_batch_adjustment")
public class SalaryBatchAdjustmentEntity extends BaseEntity {

    /**
     * 薪资批次ID。
     */
    private Long batchId;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 薪资项目编码。
     */
    private String itemCode;

    /**
     * 调整金额，正数增加、负数减少。
     */
    private BigDecimal adjustAmount;

    /**
     * 人工调整原因。
     */
    private String reason;
}

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
 * 薪资核算批次实体，对齐 hr_salary_batch 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hr_salary_batch")
public class SalaryBatchEntity extends BaseEntity {

    private String batchNo;

    private String salaryMonth;

    private String scopeType;

    private String scopeValue;

    private String batchStatus;

    private Long approvalInstanceId;

    private Integer totalCount;

    private BigDecimal totalGrossSalary;

    private BigDecimal totalNetSalary;

    private Integer yellowWarningCount;

    private Integer redWarningCount;

    private Integer blockCount;
}

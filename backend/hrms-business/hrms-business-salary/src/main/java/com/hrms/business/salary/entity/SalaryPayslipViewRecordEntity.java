package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工资条查看记录实体，对齐 hr_salary_payslip_view_record 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_payslip_view_record")
public class SalaryPayslipViewRecordEntity extends BaseEntity {

    private Long payslipItemId;

    private Long batchId;

    private Long employeeId;

    private String salaryMonth;

    private LocalDateTime firstViewTime;

    private LocalDateTime lastViewTime;

    private Integer viewCount;

    private String remark;
}

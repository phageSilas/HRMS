package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 薪资账套实体，对齐 hr_salary_template 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_template")
public class SalaryTemplateEntity extends BaseEntity {

    private String templateName;

    private String templateCode;

    private String scopeType;

    private String scopeValue;

    private LocalDate effectiveDate;

    private Integer status;

    private String remark;
}

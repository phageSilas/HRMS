package com.hrms.business.salary.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建薪资核算批次请求参数。
 */
@Data
public class SalaryBatchCreateRequestDTO {

    private String salaryMonth;

    private String month;

    private String scopeType;

    private String scopeValue;

    private List<Long> employeeIds;

    private List<Long> templateIds;
}

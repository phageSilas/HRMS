package com.hrms.business.salary.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工资条详情返回视图。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SalaryPayslipDetailVO extends SalaryBatchItemVO {

    private String salaryMonth;

    private String batchNo;
}

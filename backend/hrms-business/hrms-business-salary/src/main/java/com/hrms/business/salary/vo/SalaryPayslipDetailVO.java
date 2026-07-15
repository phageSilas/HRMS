package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
 
/**
 * 工资条详情返回视图。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SalaryPayslipDetailVO extends SalaryBatchItemVO {

    private String salaryMonth;

    private String batchNo;
}

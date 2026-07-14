package com.hrms.business.salary.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工资条验证返回视图。
 */
@Data
public class SalaryPayslipVerifyVO {

    private Boolean success;

    private String token;

    private LocalDateTime expireTime;
}

package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工资条验证返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPayslipVerifyVO {

    private Boolean success;

    private String token;

    private LocalDateTime expireTime;
}

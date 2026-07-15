package com.hrms.business.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工资条二次验证请求参数。
 */
@Data
public class SalaryPayslipVerifyRequestDTO {

    @NotBlank
    private String month;

    private String password;

    private String smsCode;
}

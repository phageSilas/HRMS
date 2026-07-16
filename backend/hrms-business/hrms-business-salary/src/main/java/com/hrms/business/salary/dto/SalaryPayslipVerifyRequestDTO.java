package com.hrms.business.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工资条二次验证请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPayslipVerifyRequestDTO {

    @NotBlank
    private String month;

    @NotBlank(message = "登录密码不能为空")
    private String password;
}

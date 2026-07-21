package com.hrms.business.salary.common.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryTemplateConstant {
    // 0
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    // 0.0000
    public static final BigDecimal ZERO_RATE = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    // 默认养老保险比例
    public static final BigDecimal DEFAULT_PENSION_INSURANCE_RATE = new BigDecimal("0.0800");
    // 默认医疗保险比例
    public static final BigDecimal DEFAULT_MEDICAL_INSURANCE_RATE = new BigDecimal("0.0200");
    // 默认失业保险比例
    public static final BigDecimal DEFAULT_UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.0050");
}

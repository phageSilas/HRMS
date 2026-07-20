package com.hrms.business.salary.common.constant;

import com.hrms.business.salary.common.enums.SalaryBatchStatusEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class SalaryCalculateConstant {
    // 默认金额
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    // 默认养老保险比例
    public static final BigDecimal DEFAULT_PENSION_INSURANCE_RATE = new BigDecimal("0.0800");
    // 默认医疗保险比例
    public static final BigDecimal DEFAULT_MEDICAL_INSURANCE_RATE = new BigDecimal("0.0200");
    // 默认失业保险比例
    public static final BigDecimal DEFAULT_UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.0050");
    // 薪资条可见状态
    public static final Set<String> PAYSLIP_VISIBLE_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVING.name(),
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name(),
            SalaryBatchStatusEnum.ARCHIVED.name()
    );
    // 薪资批次导出允许状态
    public static final Set<String> BATCH_EXPORT_ALLOWED_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name()
    );
    // 薪资管理员角色码
    public static final Set<String> SALARY_MANAGER_ROLE_CODES = Set.of(
            "FINANCE", "HR", "HR_TEST", "ADMIN", "ROLE_ADMIN"
    );
    // 薪资调整项码
    public static final Set<String> SALARY_ADJUST_ITEM_CODES = Set.of(
            "BASE_SALARY",
            "ALLOWANCE",
            "PERFORMANCE_BONUS",
            "OVERTIME_PAY",
            "LATE_DEDUCTION",
            "LEAVE_DEDUCTION",
            "SOCIAL_INSURANCE",
            "PENSION_INSURANCE",
            "MEDICAL_INSURANCE",
            "UNEMPLOYMENT_INSURANCE",
            "HOUSING_FUND",
            "INCOME_TAX"
    );
    // 薪资导出mime类型
    public static final String SALARY_EXPORT_MIME_TYPE =
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    //xlsx格式名
    public static final String SALARY_EXPORT_FILE_TYPE = "xlsx";

    //薪资批次导出业务类型
    public static final String SALARY_EXPORT_BUSINESS_TYPE = "SALARY_BATCH_EXPORT";
}

package com.hrms.business.salary.common.constant;

import com.hrms.business.salary.common.enums.SalaryBatchStatusEnum;

import java.util.Set;

public class PaySlipConstant {
    // 工资条管理 可见状态: 薪资批次状态
    public static final Set<String> PAYSLIP_VISIBLE_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVING.name(),
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name(),
            SalaryBatchStatusEnum.ARCHIVED.name()
    );
    // 工资条管理 可见状态:职位
    public static final Set<String> SALARY_MANAGER_ROLE_CODES = Set.of(
            "FINANCE", "HR", "HR_TEST", "ADMIN", "ROLE_ADMIN"
    );
    // 工资条管理 可见状态:查看状态
    public static final Set<String> PAYSLIP_MANAGE_VIEW_STATUS = Set.of(
            "VIEWED", "UNVIEWED", "UNPUBLISHED"
    );
}

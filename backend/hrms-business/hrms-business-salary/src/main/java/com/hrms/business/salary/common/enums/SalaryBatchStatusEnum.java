package com.hrms.business.salary.common.enums;

import java.util.Set;

/**
 * 薪资批次状态枚举。
 */
public enum SalaryBatchStatusEnum {
    DRAFT,
    CALCULATING,
    PENDING_REVIEW,
    APPROVING,
    APPROVED,
    RELEASED,
    ARCHIVED;

    /**
     * 判断批次是否可触发核算。
     *
     * @param status 批次状态
     * @return 是否可核算
     * 本方法使用的工具类: Set(JDK)
     */
    public static boolean canCalculate(String status) {
        return Set.of(DRAFT.name(), PENDING_REVIEW.name()).contains(status);
    }
}

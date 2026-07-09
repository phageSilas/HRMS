package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 薪资批次状态。
 */
public enum SalaryBatchStatusEnum implements BaseEnum {

    /** 草稿。 */
    DRAFT(0, "草稿"),
    /** 计算中。 */
    CALCULATING(1, "计算中"),
    /** 待确认。 */
    PENDING_CONFIRM(2, "待确认"),
    /** 已通过。 */
    APPROVED(3, "已通过"),
    /** 已发放。 */
    PAID(4, "已发放"),
    /** 已驳回。 */
    REJECTED(5, "已驳回");

    @EnumValue
    private final int code;
    private final String description;

    SalaryBatchStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

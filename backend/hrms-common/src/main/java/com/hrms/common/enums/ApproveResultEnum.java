package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 审批结果。
 */
public enum ApproveResultEnum implements BaseEnum {

    /** 通过。 */
    APPROVED(1, "通过"),
    /** 驳回。 */
    REJECTED(2, "驳回"),
    /** 转交。 */
    TRANSFERRED(3, "转交");

    @EnumValue
    private final int code;
    private final String description;

    ApproveResultEnum(int code, String description) {
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
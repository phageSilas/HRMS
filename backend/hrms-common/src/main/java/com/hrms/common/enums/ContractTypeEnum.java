package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 合同类型。
 */
public enum ContractTypeEnum implements BaseEnum {

    /** 固定期限。 */
    FIXED_TERM(1, "固定期限"),
    /** 无固定期限。 */
    NON_FIXED_TERM(2, "无固定期限"),
    /** 劳务合同。 */
    LABOR(3, "劳务合同");

    @EnumValue
    private final int code;
    private final String description;

    ContractTypeEnum(int code, String description) {
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

package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 入职类型。
 */
public enum HireTypeEnum implements BaseEnum {

    /** 全职。 */
    FULL_TIME(1, "全职"),
    /** 兼职。 */
    PART_TIME(2, "兼职"),
    /** 实习。 */
    INTERN(3, "实习");

    @EnumValue
    private final int code;
    private final String description;

    HireTypeEnum(int code, String description) {
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

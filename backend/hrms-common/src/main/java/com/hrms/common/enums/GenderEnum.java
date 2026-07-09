package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 性别。
 */
public enum GenderEnum implements BaseEnum {

    /** 男。 */
    MALE(1, "男"),
    /** 女。 */
    FEMALE(2, "女");

    @EnumValue
    private final int code;
    private final String description;

    GenderEnum(int code, String description) {
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

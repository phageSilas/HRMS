package com.hrms.common.enums;

import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum GenderEnum implements BaseEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    private final int code;

    private final String desc;

    GenderEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static GenderEnum fromCode(int code) {
        for (GenderEnum gender : values()) {
            if (gender.getCode() == code) {
                return gender;
            }
        }
        return UNKNOWN;
    }

}

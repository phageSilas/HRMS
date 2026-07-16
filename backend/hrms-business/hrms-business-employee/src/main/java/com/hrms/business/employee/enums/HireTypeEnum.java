package com.hrms.business.employee.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 入职类型枚举
 */
@Getter
public enum HireTypeEnum implements BaseEnum {

    FULL_TIME(1, "全职"),
    PART_TIME(2, "兼职"),
    INTERN(3, "实习");

    private final int code;
    private final String desc;

    HireTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static HireTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (HireTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

}

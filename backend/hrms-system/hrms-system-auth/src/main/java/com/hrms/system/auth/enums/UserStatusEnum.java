package com.hrms.system.auth.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatusEnum implements BaseEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 正常
     */
    ENABLED(1, "正常");

    private final int code;

    private final String desc;

    UserStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatusEnum fromCode(int code) {
        for (UserStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return DISABLED;
    }

}

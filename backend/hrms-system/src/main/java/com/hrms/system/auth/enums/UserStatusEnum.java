package com.hrms.system.auth.util;

/**
 * 用户状态枚举。
 */
public enum UserStatusEnum {

    /**
     * 启用。
     */
    ENABLED(1, "启用"),

    /**
     * 禁用。
     */
    DISABLED(0, "禁用");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取枚举。
     *
     * @param code 编码
     * @return 枚举值
     */
    public static UserStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断用户是否启用。
     *
     * @param code 状态编码
     * @return 启用返回 true
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.getCode().equals(code);
    }
}
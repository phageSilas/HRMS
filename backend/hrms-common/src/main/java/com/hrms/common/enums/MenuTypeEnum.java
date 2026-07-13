package com.hrms.common.enums;

import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
public enum MenuTypeEnum implements BaseEnum {

    /**
     * 目录
     */
    CATALOG(1, "目录"),

    /**
     * 菜单
     */
    MENU(2, "菜单"),

    /**
     * 按钮
     */
    BUTTON(3, "按钮");

    private final int code;

    private final String desc;

    MenuTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static MenuTypeEnum fromCode(int code) {
        for (MenuTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return CATALOG;
    }

}

package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 菜单类型。
 */
public enum MenuTypeEnum implements BaseEnum {

    /** 目录。 */
    DIR(1, "目录"),
    /** 菜单。 */
    MENU(2, "菜单"),
    /** 按钮。 */
    BUTTON(3, "按钮");

    @EnumValue
    private final int code;
    private final String description;

    MenuTypeEnum(int code, String description) {
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
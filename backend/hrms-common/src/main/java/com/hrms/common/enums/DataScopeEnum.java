package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 数据权限范围。
 */
public enum DataScopeEnum implements BaseEnum {

    /** 本人。 */
    SELF(1, "本人"),
    /** 本部门。 */
    DEPT(2, "本部门"),
    /** 本部门及子部门。 */
    DEPT_AND_CHILD(3, "本部门及子部门"),
    /** 全部。 */
    ALL(4, "全部");

    @EnumValue
    private final int code;
    private final String description;

    DataScopeEnum(int code, String description) {
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
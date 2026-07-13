package com.hrms.common.enums;

import lombok.Getter;

/**
 * 数据权限范围枚举
 */
@Getter
public enum DataScopeEnum implements BaseEnum {

    /**
     * 仅本人
     */
    OWN(1, "仅本人"),

    /**
     * 本部门
     */
    DEPT(2, "本部门"),

    /**
     * 本部门及下属
     */
    DEPT_AND_SUB(3, "本部门及下属"),

    /**
     * 全部
     */
    ALL(4, "全部");

    private final int code;

    private final String desc;

    DataScopeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static DataScopeEnum fromCode(int code) {
        for (DataScopeEnum scope : values()) {
            if (scope.getCode() == code) {
                return scope;
            }
        }
        return OWN;
    }

}

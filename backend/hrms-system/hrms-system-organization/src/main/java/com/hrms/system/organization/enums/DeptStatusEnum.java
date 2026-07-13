package com.hrms.system.organization.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 部门状态枚举
 */
@Getter
public enum DeptStatusEnum implements BaseEnum {

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

    DeptStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeptStatusEnum fromCode(int code) {
        for (DeptStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return DISABLED;
    }

}

package com.hrms.common.enums;

import lombok.Getter;

/**
 * 员工状态枚举
 */
@Getter
public enum EmployeeStatusEnum implements BaseEnum {

    /**
     * 试用期
     */
    PROBATION(1, "试用期"),

    /**
     * 正式
     */
    FORMAL(2, "正式"),

    /**
     * 离职
     */
    RESIGNED(3, "离职");

    private final int code;

    private final String desc;

    EmployeeStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static EmployeeStatusEnum fromCode(int code) {
        for (EmployeeStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return PROBATION;
    }

}

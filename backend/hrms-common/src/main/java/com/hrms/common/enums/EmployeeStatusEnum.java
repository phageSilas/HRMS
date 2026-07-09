package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 员工在职状态。
 */
public enum EmployeeStatusEnum implements BaseEnum {

    /** 试用期。 */
    PROBATION(1, "试用期"),
    /** 正式。 */
    REGULAR(2, "正式"),
    /** 待离职。 */
    RESIGNING(3, "待离职"),
    /** 已离职。 */
    RESIGNED(4, "已离职");

    @EnumValue
    private final int code;
    private final String description;

    EmployeeStatusEnum(int code, String description) {
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

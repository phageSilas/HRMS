package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 请假类型。
 */
public enum LeaveTypeEnum implements BaseEnum {

    /** 年假。 */
    ANNUAL(1, "年假"),
    /** 病假。 */
    SICK(2, "病假"),
    /** 事假。 */
    PERSONAL(3, "事假"),
    /** 婚假。 */
    MARRIAGE(4, "婚假"),
    /** 产假。 */
    MATERNITY(5, "产假"),
    /** 丧假。 */
    BEREAVEMENT(6, "丧假"),
    /** 调休。 */
    COMPENSATORY(7, "调休");

    @EnumValue
    private final int code;
    private final String description;

    LeaveTypeEnum(int code, String description) {
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

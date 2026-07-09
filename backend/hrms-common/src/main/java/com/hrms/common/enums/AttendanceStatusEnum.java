package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 每日考勤状态。
 */
public enum AttendanceStatusEnum implements BaseEnum {

    /** 正常。 */
    NORMAL(0, "正常"),
    /** 迟到。 */
    LATE(1, "迟到"),
    /** 早退。 */
    EARLY_LEAVE(2, "早退"),
    /** 旷工。 */
    ABSENT(3, "旷工"),
    /** 缺卡。 */
    MISSING_PUNCH(4, "缺卡"),
    /** 请假。 */
    LEAVE(5, "请假");

    @EnumValue
    private final int code;
    private final String description;

    AttendanceStatusEnum(int code, String description) {
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

package com.hrms.business.employee.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 在职状态枚举
 */
@Getter
public enum EmploymentStatusEnum implements BaseEnum {

    PROBATION(1, "试用期"),
    FORMAL(2, "正式"),
    PENDING_LEAVE(3, "待离职"),
    LEFT(4, "已离职");

    private final int code;
    private final String desc;

    EmploymentStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static EmploymentStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EmploymentStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

}

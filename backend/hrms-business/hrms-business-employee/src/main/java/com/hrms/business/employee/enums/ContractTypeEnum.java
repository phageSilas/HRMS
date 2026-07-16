package com.hrms.business.employee.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 合同类型枚举
 */
@Getter
public enum ContractTypeEnum implements BaseEnum {

    FIXED_TERM(1, "固定期限"),
    OPEN_ENDED(2, "无固定期限"),
    LABOR(3, "劳务合同");

    private final int code;
    private final String desc;

    ContractTypeEnum(int code, String desc) {
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
    public static ContractTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContractTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

}

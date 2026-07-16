package com.hrms.business.personnel.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 转正评估结果枚举
 */
@Getter
public enum RegularEvaluateResultEnum implements BaseEnum {

    /**
     * 转正
     */
    PASS(1, "pass", "转正"),

    /**
     * 延长试用
     */
    EXTEND(2, "extend", "延长试用"),

    /**
     * 辞退
     */
    TERMINATE(3, "terminate", "辞退");

    private final int code;

    private final String value;

    private final String desc;

    RegularEvaluateResultEnum(int code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据前端评估结果值获取枚举。
     *
     * @param value 前端评估结果值
     * @return 评估结果枚举
     * 本方法使用的工具类: 无
     */
    public static RegularEvaluateResultEnum fromValue(String value) {
        for (RegularEvaluateResultEnum result : values()) {
            if (result.getValue().equals(value)) {
                return result;
            }
        }
        throw new IllegalArgumentException("转正评估结果不正确");
    }

}

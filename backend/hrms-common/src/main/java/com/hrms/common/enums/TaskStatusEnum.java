package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 审批任务状态。
 */
public enum TaskStatusEnum implements BaseEnum {

    /** 待处理。 */
    PENDING(0, "待处理"),
    /** 已处理。 */
    PROCESSED(1, "已处理"),
    /** 已转交。 */
    TRANSFERRED(2, "已转交");

    @EnumValue
    private final int code;
    private final String description;

    TaskStatusEnum(int code, String description) {
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
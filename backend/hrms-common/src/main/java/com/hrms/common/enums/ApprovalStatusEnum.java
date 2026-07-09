package com.hrms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 审批单据通用状态。
 */
public enum ApprovalStatusEnum implements BaseEnum {

    /** 草稿。 */
    DRAFT(0, "草稿"),
    /** 审批中。 */
    APPROVING(1, "审批中"),
    /** 已通过。 */
    APPROVED(2, "已通过"),
    /** 已驳回。 */
    REJECTED(3, "已驳回"),
    /** 已撤回。 */
    WITHDRAWN(4, "已撤回");

    @EnumValue
    private final int code;
    private final String description;

    ApprovalStatusEnum(int code, String description) {
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

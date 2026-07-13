package com.hrms.common.enums;

import lombok.Getter;

/**
 * 审批状态枚举
 */
@Getter
public enum ApprovalStatusEnum implements BaseEnum {

    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 审批中
     */
    APPROVING(1, "审批中"),

    /**
     * 已通过
     */
    APPROVED(2, "已通过"),

    /**
     * 已驳回
     */
    REJECTED(3, "已驳回"),

    /**
     * 已撤回
     */
    WITHDRAWN(4, "已撤回");

    private final int code;

    private final String desc;

    ApprovalStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static ApprovalStatusEnum fromCode(int code) {
        for (ApprovalStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return DRAFT;
    }

}

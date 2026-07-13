package com.hrms.business.personnel.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 申请状态枚举
 */
@Getter
public enum ApplicationStatusEnum implements BaseEnum {

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
    WITHDRAWN(4, "已撤回"),

    /**
     * 已入职
     */
    ENTERED(5, "已入职");

    private final int code;

    private final String desc;

    ApplicationStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApplicationStatusEnum fromCode(int code) {
        for (ApplicationStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return DRAFT;
    }

    /**
     * 根据状态码获取状态描述。
     *
     * @param code 状态码
     * @return 状态描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return DRAFT.getDesc();
        }
        return fromCode(code).getDesc();
    }

}

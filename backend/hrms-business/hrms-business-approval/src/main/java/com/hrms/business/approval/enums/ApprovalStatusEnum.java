package com.hrms.business.approval.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 审批实例状态枚举
 * <p>
 * 对应 hr_approval_instance.approval_status 字段
 * </p>
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
    PENDING(1, "审批中"),

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
     * @param code 编码值
     * @return 枚举，未找到返回 null
     */
    public static ApprovalStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatusEnum item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
}

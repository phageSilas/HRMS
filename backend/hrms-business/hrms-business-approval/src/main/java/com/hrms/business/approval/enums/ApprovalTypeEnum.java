package com.hrms.business.approval.enums;

import lombok.Getter;

/**
 * 审批类型枚举
 * <p>
 * 对应 hr_approval_instance.approval_type 字段（VARCHAR 类型）
 * 注：该枚举不实现 BaseEnum，因为类型编码为字符串而非整数
 * </p>
 */
@Getter
public enum ApprovalTypeEnum {

    /**
     * 入职审批
     */
    ENTRY("ENTRY", "入职审批"),

    /**
     * 转正审批
     */
    REGULAR("REGULAR", "转正审批"),

    /**
     * 调岗审批
     */
    TRANSFER("TRANSFER", "调岗审批"),

    /**
     * 离职审批
     */
    LEAVE("LEAVE", "离职审批"),

    /**
     * 请假审批
     */
    LEAVE_REQUEST("LEAVE_REQUEST", "请假审批"),

    /**
     * 补卡审批
     */
    CORRECTION("CORRECTION", "补卡审批"),

    /**
     * 薪资批次审批
     */
    SALARY("SALARY", "薪资批次审批");

    /**
     * 类型编码（与 DB 存储值一致）
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String desc;

    ApprovalTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码（如 "ENTRY"）
     * @return 枚举，未找到返回 null
     */
    public static ApprovalTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ApprovalTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}

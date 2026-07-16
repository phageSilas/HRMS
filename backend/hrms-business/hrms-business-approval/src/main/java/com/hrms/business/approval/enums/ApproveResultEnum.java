package com.hrms.business.approval.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 审批结果枚举
 * <p>
 * 对应 hr_approval_task.approve_result 字段
 * </p>
 */
@Getter
public enum ApproveResultEnum implements BaseEnum {

    /**
     * 通过
     */
    APPROVE(1, "通过"),

    /**
     * 驳回
     */
    REJECT(2, "驳回"),

    /**
     * 转交
     */
    TRANSFER(3, "转交");

    private final int code;
    private final String desc;

    ApproveResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码值
     * @return 枚举，未找到返回 null
     */
    public static ApproveResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApproveResultEnum item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
}

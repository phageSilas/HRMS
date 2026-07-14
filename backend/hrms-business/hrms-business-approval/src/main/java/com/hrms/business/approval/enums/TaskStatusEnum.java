package com.hrms.business.approval.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 审批任务状态枚举
 * <p>
 * 对应 hr_approval_task.task_status 字段
 * </p>
 */
@Getter
public enum TaskStatusEnum implements BaseEnum {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已处理
     */
    PROCESSED(1, "已处理"),

    /**
     * 已转交
     */
    TRANSFERRED(2, "已转交");

    private final int code;
    private final String desc;

    TaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码值
     * @return 枚举，未找到返回 null
     */
    public static TaskStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatusEnum item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
}

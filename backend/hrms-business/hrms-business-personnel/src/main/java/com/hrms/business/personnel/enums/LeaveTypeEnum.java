package com.hrms.business.personnel.enums;

import com.hrms.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 离职类型枚举
 */
@Getter
public enum LeaveTypeEnum implements BaseEnum {

    /**
     * 主动辞职
     */
    RESIGN(1, "resign", "主动辞职"),

    /**
     * 被动辞退
     */
    TERMINATE(2, "terminate", "被动辞退"),

    /**
     * 合同到期不续签
     */
    CONTRACT_END(3, "contract_end", "合同到期不续签"),

    /**
     * 协商解除
     */
    MUTUAL(4, "mutual", "协商解除");

    private final int code;

    private final String value;

    private final String desc;

    LeaveTypeEnum(int code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据前端离职类型值获取枚举。
     *
     * @param value 前端离职类型值
     * @return 离职类型枚举
     * 本方法使用的工具类: 无
     */
    public static LeaveTypeEnum fromValue(String value) {
        for (LeaveTypeEnum leaveType : values()) {
            if (leaveType.getValue().equals(value)) {
                return leaveType;
            }
        }
        throw new IllegalArgumentException("离职类型不正确");
    }

    /**
     * 根据编码获取前端离职类型值。
     *
     * @param code 离职类型编码
     * @return 前端离职类型值
     * 本方法使用的工具类: 无
     */
    public static String getValueByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LeaveTypeEnum leaveType : values()) {
            if (leaveType.getCode() == code) {
                return leaveType.getValue();
            }
        }
        return null;
    }

    /**
     * 根据编码获取离职类型描述。
     *
     * @param code 离职类型编码
     * @return 离职类型描述
     * 本方法使用的工具类: 无
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LeaveTypeEnum leaveType : values()) {
            if (leaveType.getCode() == code) {
                return leaveType.getDesc();
            }
        }
        return null;
    }

}

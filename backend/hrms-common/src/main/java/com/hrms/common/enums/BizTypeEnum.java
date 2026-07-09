package com.hrms.common.enums;

/**
 * 审批业务类型。
 *
 * <p>遵循全局技术底座契约：该枚举使用字符串常量编码，不使用数字编码。</p>
 */
public enum BizTypeEnum {

    /** 入职。 */
    ONBOARDING("ONBOARDING", "入职"),
    /** 调岗。 */
    TRANSFER("TRANSFER", "调岗"),
    /** 离职。 */
    DIMISSION("DIMISSION", "离职"),
    /** 请假。 */
    LEAVE("LEAVE", "请假"),
    /** 补卡。 */
    ATTENDANCE_RECTIFY("ATTENDANCE_RECTIFY", "补卡"),
    /** 薪资审批。 */
    SALARY_APPROVAL("SALARY_APPROVAL", "薪资审批");

    private final String code;
    private final String description;

    BizTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取字符串编码。
     *
     * @return 编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取描述文字。
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }
}

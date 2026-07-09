package com.hrms.common.enums;

/**
 * 数字编码枚举的公共契约。
 *
 * <p>遵循全局技术底座契约：枚举 Key 必须使用数字编码（对应 TINYINT），
 * 描述文字仅供前端展示。</p>
 */
public interface BaseEnum {

    /**
     * 获取数字编码。
     *
     * @return 编码
     */
    Integer getCode();

    /**
     * 获取描述文字。
     *
     * @return 描述
     */
    String getDescription();
}

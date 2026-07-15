package com.hrms.business.employee.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 敏感数据脱敏工具类
 * <p>
 * 用于身份证号、手机号、银行卡号等敏感字段的脱敏显示
 * </p>
 */
@Slf4j
public class DesensitizationUtil {

    private DesensitizationUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 身份证号脱敏
     * <p>
     * 保留前4位和后4位，中间用 * 号代替
     * 示例：330***********1234 → 330****1234
     * </p>
     *
     * @param idCardNo 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCardNo(String idCardNo) {
        if (idCardNo == null || idCardNo.isEmpty()) {
            return idCardNo;
        }
        if (idCardNo.length() <= 8) {
            return idCardNo;
        }
        String prefix = idCardNo.substring(0, 4);
        String suffix = idCardNo.substring(idCardNo.length() - 4);
        return prefix + "****" + suffix;
    }

    /**
     * 手机号脱敏
     * <p>
     * 保留前3位和后4位，中间用 * 号代替
     * 示例：138****1234
     * </p>
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() <= 7) {
            return phone;
        }
        String prefix = phone.substring(0, 3);
        String suffix = phone.substring(phone.length() - 4);
        return prefix + "****" + suffix;
    }

    /**
     * 银行卡号脱敏
     * <p>
     * 仅显示后4位，前面用 * 号代替
     * 示例：**** **** **** 1234
     * </p>
     *
     * @param bankAccount 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.isEmpty()) {
            return bankAccount;
        }
        if (bankAccount.length() <= 4) {
            return bankAccount;
        }
        String suffix = bankAccount.substring(bankAccount.length() - 4);
        return "**** **** **** " + suffix;
    }

    /**
     * 通用脱敏
     * <p>
     * 保留前缀和后缀长度，中间用 * 号代替
     * </p>
     *
     * @param value        原始值
     * @param prefixLength 前缀保留长度
     * @param suffixLength 后缀保留长度
     * @return 脱敏后的值
     */
    public static String desensitize(String value, int prefixLength, int suffixLength) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= prefixLength + suffixLength) {
            return value;
        }
        String prefix = value.substring(0, prefixLength);
        String suffix = value.substring(value.length() - suffixLength);
        return prefix + "****" + suffix;
    }

}

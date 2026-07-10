package com.hrms.system.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类。
 *
 * <p>使用 BCrypt 算法进行密码加密和验证。</p>
 */
@Component
public class PasswordUtils {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码。
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码。
     *
     * @param rawPassword       原始密码
     * @param encodedPassword   加密后的密码
     * @return 匹配返回 true
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
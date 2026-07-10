package com.hrms.system.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类。
 *
 * <p>用于生成 BCrypt 加密的密码。</p>
 */
public class PasswordGenerator {

    /**
     * 生成 BCrypt 加密的密码。
     *
     * @param args 命令行参数，第一个参数为原始密码
     */
    public static void main(String[] args) {
        String rawPassword;

        if (args.length > 0) {
            rawPassword = args[0];
        } else {
            // 默认密码
            rawPassword = "Admin@2026!HRMS";
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("========================================");
        System.out.println("原始密码：" + rawPassword);
        System.out.println("加密密码：" + encodedPassword);
        System.out.println("========================================");
        System.out.println();
        System.out.println("SQL 更新语句：");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("验证密码是否匹配：" + encoder.matches(rawPassword, encodedPassword));
    }
}
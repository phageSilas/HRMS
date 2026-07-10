package com.hrms.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试环境启动类
 * 仅用于测试环境，扫描 hrms-system 模块的所有组件
 *
 * @author HRMS Team
 */
@SpringBootApplication(scanBasePackages = "com.hrms.system")
public class HrmsSystemTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsSystemTestApplication.class, args);
    }
}
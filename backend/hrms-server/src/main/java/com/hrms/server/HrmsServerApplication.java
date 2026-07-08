package com.hrms.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动 HRMS 单体多模块应用。
 */
@SpringBootApplication(scanBasePackages = "com.hrms")
public class HrmsServerApplication {

    /**
     * 启动 HRMS 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HrmsServerApplication.class, args);
    }
}

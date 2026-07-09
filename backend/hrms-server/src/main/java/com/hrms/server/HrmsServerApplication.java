package com.hrms.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动 HRMS 单体多模块应用。
 */
@SpringBootApplication(scanBasePackages = "com.hrms")
@MapperScan({"com.hrms.system.mapper", "com.hrms.business.mapper"})
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

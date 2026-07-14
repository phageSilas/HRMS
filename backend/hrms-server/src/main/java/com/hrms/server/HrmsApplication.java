package com.hrms.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HRMS 应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.hrms"})
@EnableAsync
@EnableScheduling
public class HrmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);
        System.out.println("========================================");
        System.out.println("HRMS Server 启动成功！");
        System.out.println("========================================");
    }

}

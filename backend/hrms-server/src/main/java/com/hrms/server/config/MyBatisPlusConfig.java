package com.hrms.server.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
@MapperScan(basePackages = {"com.hrms.**.mapper"})
public class MyBatisPlusConfig {

}

package com.hrms.business.approval.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 审批中心模块配置
 */
@Configuration
@MapperScan("com.hrms.business.approval.mapper")
public class ApprovalConfig {

}

package com.hrms.system;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试基类
 * 所有测试类都应继承此类，确保测试环境统一配置
 *
 * @author HRMS Team
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = HrmsSystemTestApplication.class)
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {
    // 测试基类，提供通用配置
    // 子类可以继承此类，自动获得：
    // 1. Spring Boot 测试环境
    // 2. 测试配置（application-test.yaml）
    // 3. 事务回滚（每个测试方法执行后自动回滚）
}
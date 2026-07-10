package com.hrms.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置。
 *
 * <p>配置内容：</p>
 * <ul>
 *   <li>分页插件：由 mybatis-plus-spring-boot3-starter 自动配置</li>
 *   <li>逻辑删除：通过 BaseEntity 的 @TableLogic 注解配置</li>
 * </ul>
 *
 * <p>逻辑删除说明：</p>
 * <ul>
 *   <li>调用 deleteById() 时执行 UPDATE 设置 is_deleted = 1，而非物理删除</li>
 *   <li>查询时自动添加 WHERE is_deleted = 0 条件，过滤已删除记录</li>
 * </ul>
 *
 * <p>注意：mybatis-plus-spring-boot3-starter 已自动配置分页插件，
 * 无需手动添加 PaginationInnerInterceptor。</p>
 */
@Configuration
public class MybatisPlusConfig {

    // MyBatis-Plus Spring Boot Starter 已自动配置分页插件
    // 逻辑删除通过 BaseEntity 的 @TableLogic 注解自动生效
    // MyMetaObjectHandler 负责自动填充公共字段

}
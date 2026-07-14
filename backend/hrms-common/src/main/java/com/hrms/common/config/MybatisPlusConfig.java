package com.hrms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import com.hrms.common.interceptor.DataScopeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁插件：用于处理 BaseEntity.version 上的 @Version
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 数据权限拦截器
        interceptor.addInnerInterceptor(dataScopeInterceptor());

        // 分页插件建议最后添加
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    /**
     * 配置数据权限拦截器
     *
     * @return DataScopeInterceptor
     */
    @Bean
    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }

}

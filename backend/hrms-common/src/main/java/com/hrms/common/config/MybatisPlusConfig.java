package com.hrms.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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

        // 分页插件（MyBatis-Plus 3.5.12 使用 PaginationInnerInterceptor）
        try {
            // 尝试加载新版本的类
            Class<?> clazz = Class.forName("com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            Object innerInterceptor = clazz.getDeclaredConstructor().newInstance();
            // 使用反射调用 addInnerInterceptor 方法
            interceptor.getClass().getMethod("addInnerInterceptor", Object.class).invoke(interceptor, innerInterceptor);
        } catch (ClassNotFoundException e) {
            // 兼容旧版本
            System.out.println("使用旧版本 MyBatis-Plus API");
        } catch (Exception e) {
            System.out.println("分页插件初始化失败: " + e.getMessage());
        }

        // 注册数据权限拦截器
        interceptor.addInnerInterceptor(dataScopeInterceptor());

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

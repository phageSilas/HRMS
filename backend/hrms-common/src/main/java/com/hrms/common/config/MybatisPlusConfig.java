package com.hrms.common.config;

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

        // 分页插件
        addPaginationInterceptor(interceptor);

        // 乐观锁插件（配合实体 @Version 注解使用）
        addOptimisticLockerInterceptor(interceptor);

        // 数据权限拦截器
        interceptor.addInnerInterceptor(dataScopeInterceptor());

        return interceptor;
    }

    /**
     * 反射方式注册分页拦截器，兼容不同 MyBatis-Plus 版本。
     */
    private void addPaginationInterceptor(MybatisPlusInterceptor interceptor) {
        try {
            Class<?> clazz = Class.forName(
                    "com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            Object innerInterceptor = clazz.getDeclaredConstructor().newInstance();
            interceptor.getClass()
                    .getMethod("addInnerInterceptor", innerInterceptor.getClass().getInterfaces()[0])
                    .invoke(interceptor, innerInterceptor);
        } catch (Exception e) {
            System.out.println("分页插件初始化失败: " + e.getMessage());
        }
    }

    /**
     * 反射方式注册乐观锁拦截器，兼容不同 MyBatis-Plus 版本。
     */
    private void addOptimisticLockerInterceptor(MybatisPlusInterceptor interceptor) {
        try {
            Class<?> clazz = Class.forName(
                    "com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor");
            Object innerInterceptor = clazz.getDeclaredConstructor().newInstance();
            interceptor.getClass()
                    .getMethod("addInnerInterceptor", innerInterceptor.getClass().getInterfaces()[0])
                    .invoke(interceptor, innerInterceptor);
        } catch (Exception e) {
            System.out.println("乐观锁插件初始化失败: " + e.getMessage());
        }
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

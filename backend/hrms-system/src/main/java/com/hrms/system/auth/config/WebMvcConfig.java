package com.hrms.system.auth.config;

import com.hrms.system.auth.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>注册认证拦截器，配置拦截路径。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除登录接口
                .excludePathPatterns(
                        "/auth/login",
                        "/error",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
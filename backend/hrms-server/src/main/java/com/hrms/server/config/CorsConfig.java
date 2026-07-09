package com.hrms.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置。
 *
 * <p>允许前端（React 应用）跨域调用后端接口。</p>
 *
 * <p>配置内容：</p>
 * <ul>
 *   <li>Access-Control-Allow-Origin: 允许所有来源（开发环境）</li>
 *   <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS</li>
 *   <li>Access-Control-Allow-Headers: Authorization, Content-Type</li>
 *   <li>Access-Control-Allow-Credentials: true</li>
 * </ul>
 *
 * <p>生产环境建议通过配置文件限制允许的来源。</p>
 */
@Configuration
public class CorsConfig {

    /**
     * 配置 CORS 过滤器。
     *
     * @return CorsFilter CORS 过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（开发环境）
        // 生产环境应改为具体域名，如：config.addAllowedOrigin("https://hrms.example.com");
        config.addAllowedOriginPattern("*");

        // 允许的请求方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");

        // 允许的请求头
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("*");

        // 允许携带凭证（Cookie）
        config.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 映射路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
package com.hrms.system.auth.config;

import com.hrms.system.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 无状态不需要）
            .csrf(AbstractHttpConfigurer::disable)

            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)

            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)

            // 配置 Session 策略为 STATELESS
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开路径
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/logout",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/static/**",
                    "/favicon.ico"
                ).permitAll()
                // 其他所有路径需要认证
                .anyRequest().authenticated()
            )

            // 配置异常处理
            .exceptionHandling(exception -> {
                exception.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
            })

            // 添加 JWT 认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置 BCryptPasswordEncoder（Cost=10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}

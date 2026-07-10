package com.hrms.system.auth.filter;

import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import com.hrms.system.auth.util.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * 认证上下文过滤器。
 *
 * <p>将用户信息从 Token 中解析并存入 SecurityContextHolder。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthContextFilter implements Filter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 1. 从请求头获取 Token
            String token = httpRequest.getHeader("Authorization");

            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 2. 如果有 Token 且有效，解析用户信息
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                List<Long> roleIds = jwtUtils.getRoleIdsFromToken(token);

                // 3. 创建 UserContext 并存入 SecurityContextHolder
                UserContext userContext = new UserContext(userId, null, roleIds);
                SecurityContextHolder.setContext(userContext);
            }

            // 4. 继续执行过滤器链
            chain.doFilter(request, response);

        } finally {
            // 5. 清理 ThreadLocal，避免内存泄漏
            SecurityContextHolder.clear();
        }
    }
}
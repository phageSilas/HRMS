package com.hrms.system.auth.interceptor;

import com.hrms.common.security.UserContext;
import com.hrms.system.auth.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 认证拦截器。
 *
 * <p>拦截请求，解析 Token，校验权限。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取 Token
        String token = request.getHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 如果没有 Token，允许继续（某些接口允许匿名访问）
        if (!StringUtils.hasText(token)) {
            return true;
        }

        // 3. 验证 Token
        if (!jwtUtils.validateToken(token)) {
            return true; // Token 无效，但不拦截（由 SecurityContextHolder 判断）
        }

        // 4. 解析 Token 获取用户信息
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        List<Long> roleIds = jwtUtils.getRoleIdsFromToken(token);
        List<String> permissions = jwtUtils.getPermissionsFromToken(token);

        // 5. 将用户信息存入 ThreadLocal
        UserContext userContext = new UserContext(userId, null, roleIds);
        request.setAttribute("userContext", userContext);
        request.setAttribute("username", username);
        request.setAttribute("permissions", permissions);

        return true;
    }
}
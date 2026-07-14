package com.hrms.system.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.JwtUtils;
import com.hrms.common.security.UserContext;
import com.hrms.common.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    // 内存黑名单（开发环境替代 Redis）
    private static final ConcurrentHashMap<String, Boolean> tokenBlacklist = new ConcurrentHashMap<>();
    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 检查 Token 是否在黑名单中
            if (isTokenBlacklisted(token)) {
                throw new GlobalException(ErrorCode.TOKEN_INVALID);
            }

            // 验证 Token 是否过期
            if (jwtUtils.isTokenExpired(token)) {
                throw new GlobalException(ErrorCode.TOKEN_EXPIRED);
            }

            // 验证 Token 签名
            if (!jwtUtils.validateToken(token)) {
                throw new GlobalException(ErrorCode.TOKEN_INVALID);
            }

            // 解析 Token 中的用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            Long deptId = jwtUtils.getDeptIdFromToken(token);
            List<Long> roleIds = jwtUtils.getRoleIdsFromToken(token);

            // 创建用户上下文
            UserContext userContext = new UserContext();
            userContext.setUserId(userId);
            userContext.setUsername(username);
            userContext.setDeptId(deptId);
            userContext.setRoleIds(roleIds);

            // 注入到自定义 SecurityContextHolder
            com.hrms.common.security.SecurityContextHolder.setContext(userContext);

            // 注入到 Spring Security Context
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userContext,
                    null,
                    Collections.emptyList()
                );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);
        } catch (GlobalException e) {
            handleAuthenticationError(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("JWT 认证失败: {}", e.getMessage(), e);
            handleAuthenticationError(response, ErrorCode.TOKEN_INVALID);
        } finally {
            // 请求结束后清除上下文
            com.hrms.common.security.SecurityContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 检查 Token 是否在黑名单中（内存实现，开发环境）
     */
    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_KEY + token;
        return tokenBlacklist.containsKey(key);
    }

    /**
     * 处理认证错误
     */
    private void handleAuthenticationError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.error(errorCode);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }

}

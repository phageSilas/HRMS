package com.hrms.system.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类。
 *
 * <p>提供 JWT Token 的生成、解析和验证功能。</p>
 */
@Component
public class JwtUtils {

    /**
     * JWT 密钥。
     */
    @Value("${jwt.secret:hrms-jwt-secret-key-2024-must-be-at-least-256-bits-long}")
    private String secret;

    /**
     * JWT 过期时间（毫秒），默认 7 天。
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * 生成 JWT Token。
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param roleIds     角色 ID 列表
     * @param permissions 权限标识列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, List<Long> roleIds, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roleIds", roleIds)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 JWT Token。
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 JWT Token 是否有效。
     *
     * @param token JWT Token
     * @return 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户 ID。
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户名。
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取角色 ID 列表。
     *
     * @param token JWT Token
     * @return 角色 ID 列表
     */
    @SuppressWarnings("unchecked")
    public List<Long> getRoleIdsFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleIds", List.class);
    }

    /**
     * 从 Token 中获取权限标识列表。
     *
     * @param token JWT Token
     * @return 权限标识列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions", List.class);
    }

    /**
     * 检查 Token 是否过期。
     *
     * @param token JWT Token
     * @return 过期返回 true，否则返回 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
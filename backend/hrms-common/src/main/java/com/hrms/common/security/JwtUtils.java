package com.hrms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JWT工具类（Spring Bean 方式）
 */
@Slf4j
@Component
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_DEPT_ID = "deptId";
    private static final String CLAIM_ROLE_IDS = "roleIds";

    @Value("${hrms.jwt.secret}")
    private String secretKey;

    @Value("${hrms.jwt.expiration:7200000}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param deptId   部门ID
     * @param roleIds  角色ID列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, Long deptId, List<Long> roleIds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_DEPT_ID, deptId)
                .claim(CLAIM_ROLE_IDS, roleIds)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中解析Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    /**
     * 从Token中提取部门ID
     *
     * @param token JWT Token
     * @return 部门ID
     */
    public Long getDeptIdFromToken(String token) {
        return parseClaims(token).get(CLAIM_DEPT_ID, Long.class);
    }

    /**
     * 从Token中提取角色ID列表
     *
     * @param token JWT Token
     * @return 角色ID列表
     */
    @SuppressWarnings("unchecked")
    public List<Long> getRoleIdsFromToken(String token) {
        return parseClaims(token).get(CLAIM_ROLE_IDS, List.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取Token过期时间（毫秒）
     *
     * @return 过期时间
     */
    public long getExpiration() {
        return expiration;
    }

}

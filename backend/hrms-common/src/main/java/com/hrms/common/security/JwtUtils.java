package com.hrms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_DEPT_ID = "deptId";
    private static final String CLAIM_ROLE_IDS = "roleIds";

    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时

    /**
     * 生成Token
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param deptId      部门ID
     * @param roleIds     角色ID列表
     * @param secretKey   密钥
     * @return JWT Token
     */
    public static String generateToken(Long userId, String username, Long deptId, List<Long> roleIds, String secretKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_DEPT_ID, deptId)
                .claim(CLAIM_ROLE_IDS, roleIds)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中解析Claims
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return Claims
     */
    public static Claims parseClaims(String token, String secretKey) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token, String secretKey) {
        return parseClaims(token, secretKey).get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 从Token中提取用户名
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 用户名
     */
    public static String getUsernameFromToken(String token, String secretKey) {
        return parseClaims(token, secretKey).get(CLAIM_USERNAME, String.class);
    }

    /**
     * 从Token中提取部门ID
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 部门ID
     */
    public static Long getDeptIdFromToken(String token, String secretKey) {
        return parseClaims(token, secretKey).get(CLAIM_DEPT_ID, Long.class);
    }

    /**
     * 从Token中提取角色ID列表
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 角色ID列表
     */
    public static List<Long> getRoleIdsFromToken(String token, String secretKey) {
        return parseClaims(token, secretKey).get(CLAIM_ROLE_IDS, List.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, String secretKey) {
        try {
            Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token是否过期
     *
     * @param token     JWT Token
     * @param secretKey 密钥
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token, String secretKey) {
        try {
            Claims claims = parseClaims(token, secretKey);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}

package com.hrms.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 服务工具类
 * 提供 Token 黑名单、登录失败记录、用户会话等功能的 Redis 操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== Token 黑名单 ====================

    /**
     * 将 Token 加入黑名单
     *
     * @param token Token
     * @param ttl   过期时间（秒）
     */
    public void addTokenBlacklist(String token, long ttl) {
        String key = "hrms:token:blacklist:" + token;
        redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
        log.debug("Token 加入黑名单: {}", token);
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token Token
     * @return true-在黑名单中 false-不在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String key = "hrms:token:blacklist:" + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 从黑名单中移除 Token
     *
     * @param token Token
     */
    public void removeTokenFromBlacklist(String token) {
        String key = "hrms:token:blacklist:" + token;
        redisTemplate.delete(key);
    }

    // ==================== 登录失败记录 ====================

    /**
     * 记录登录失败次数
     *
     * @param username 用户名
     * @param ttl      过期时间（秒）
     * @return 当前失败次数
     */
    public long recordLoginFailed(String username, long ttl) {
        String key = "hrms:login:failed:" + username;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        log.debug("记录登录失败: {}, 次数: {}", username, count);
        return count != null ? count : 0;
    }

    /**
     * 获取登录失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public long getLoginFailedCount(String username) {
        String key = "hrms:login:failed:" + username;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 清除登录失败记录
     *
     * @param username 用户名
     */
    public void clearLoginFailed(String username) {
        String key = "hrms:login:failed:" + username;
        redisTemplate.delete(key);
        log.debug("清除登录失败记录: {}", username);
    }

    // ==================== 用户会话 ====================

    /**
     * 保存用户会话
     *
     * @param userId 用户ID
     * @param token  Token
     * @param ttl    过期时间（秒）
     */
    public void saveUserSession(Long userId, String token, long ttl) {
        String key = "hrms:session:" + userId;
        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
        log.debug("保存用户会话: userId={}, token={}", userId, token);
    }

    /**
     * 获取用户会话 Token
     *
     * @param userId 用户ID
     * @return Token
     */
    public String getUserSession(Long userId) {
        String key = "hrms:session:" + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 删除用户会话
     *
     * @param userId 用户ID
     */
    public void removeUserSession(Long userId) {
        String key = "hrms:session:" + userId;
        redisTemplate.delete(key);
        log.debug("删除用户会话: userId={}", userId);
    }

    /**
     * 刷新用户会话过期时间
     *
     * @param userId 用户ID
     * @param ttl    过期时间（秒）
     */
    public void refreshUserSession(Long userId, long ttl) {
        String key = "hrms:session:" + userId;
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    // ==================== 通用操作 ====================

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     * @param ttl   过期时间（秒）
     */
    public void set(String key, Object value, long ttl) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除键
     *
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return true-存在 false-不存在
     */
    public boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

}
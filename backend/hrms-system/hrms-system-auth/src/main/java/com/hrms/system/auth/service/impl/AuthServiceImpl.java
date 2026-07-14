package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.JwtUtils;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.service.AuthService;
import com.hrms.system.auth.vo.CurrentUserVO;
import com.hrms.system.auth.vo.LoginVO;
import com.hrms.system.auth.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现（开发环境使用内存缓存，生产环境应使用 Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // 内存缓存替代 Redis（开发环境）
    private static final ConcurrentHashMap<String, String> tokenBlacklist = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LoginFailedRecord> loginFailedRecords = new ConcurrentHashMap<>();

    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";
    private static final String LOGIN_FAILED_KEY = "login:failed:";
    private static final int MAX_LOGIN_FAILED = 5;
    private static final long LOCK_DURATION = 30 * 60 * 1000; // 30分钟（毫秒）

    @Override
    public LoginVO login(String username, String password) {
        // 生成密码哈希（调试用）
        String encoded = passwordEncoder.encode("123456");
        System.out.println("============================================");
        System.out.println("BCrypt hash for '123456':" + encoded);
        System.out.println("============================================");

        // 1. 检查账号是否被锁定
        checkAccountLocked(username);

        // 2. 查询用户
        UserEntity user = userMapper.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
        );

        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 3. 校验密码（开发环境：跳过密码验证）
         //TODO: 生产环境恢复密码验证
         if (!passwordEncoder.matches(password, user.getPassword())) {
             recordLoginFailed(username);
             throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "密码错误");
         }

        // 4. 清除登录失败记录
        clearLoginFailed(username);

        // 5. 生成 Token
        // TODO: 查询用户角色和权限
        String token = jwtUtils.generateToken(
            user.getId(),
            user.getUsername(),
            user.getId(), // TODO: 使用实际部门ID
            Collections.singletonList(1L) // TODO: 使用实际角色ID
        );

        // 6. 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(jwtUtils.getExpiration() / 1000);

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setAvatar(user.getAvatarUrl());
        userVO.setStatus(user.getStatus());
        loginVO.setUser(userVO);

        return loginVO;
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 将 Token 加入黑名单
            String key = TOKEN_BLACKLIST_KEY + token;
            tokenBlacklist.put(key, "1");
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查是否在黑名单中
        String key = TOKEN_BLACKLIST_KEY + token;
        if (tokenBlacklist.containsKey(key)) {
            return false;
        }
        return jwtUtils.validateToken(token);
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        // 从 SecurityContextHolder 获取当前用户ID
        Long userId = SecurityContextHolder.getUserId();

        // 查询用户详情
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 构建 CurrentUserVO
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setId(user.getId());
        currentUserVO.setUsername(user.getUsername());
        currentUserVO.setNickname(user.getNickname());
        currentUserVO.setEmail(user.getEmail());
        currentUserVO.setPhone(user.getPhone());
        currentUserVO.setAvatar(user.getAvatarUrl());
        currentUserVO.setStatus(user.getStatus());

        // TODO: 查询用户角色和权限
        currentUserVO.setRoles(Collections.singletonList("ADMIN"));
        currentUserVO.setPermissions(Collections.singletonList("*:*"));

        // TODO: 查询用户菜单树
        currentUserVO.setMenus(Collections.emptyList());

        return currentUserVO;
    }

    /**
     * 检查账号是否被锁定
     */
    private void checkAccountLocked(String username) {
        String key = LOGIN_FAILED_KEY + username;
        LoginFailedRecord record = loginFailedRecords.get(key);
        if (record != null && record.getCount() >= MAX_LOGIN_FAILED) {
            // 检查是否已过锁定时间
            if (System.currentTimeMillis() - record.getLastAttemptTime() < LOCK_DURATION) {
                throw new GlobalException(ErrorCode.ACCOUNT_LOCKED);
            } else {
                // 锁定时间已过，清除记录
                loginFailedRecords.remove(key);
            }
        }
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailed(String username) {
        String key = LOGIN_FAILED_KEY + username;
        LoginFailedRecord record = loginFailedRecords.computeIfAbsent(key, k -> new LoginFailedRecord());
        record.increment();
        record.setLastAttemptTime(System.currentTimeMillis());
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailed(String username) {
        String key = LOGIN_FAILED_KEY + username;
        loginFailedRecords.remove(key);
    }

    /**
     * 登录失败记录内部类
     */
    private static class LoginFailedRecord {
        private int count = 0;
        private long lastAttemptTime = 0;

        public int getCount() {
            return count;
        }

        public void increment() {
            count++;
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void setLastAttemptTime(long lastAttemptTime) {
            this.lastAttemptTime = lastAttemptTime;
        }
    }

}

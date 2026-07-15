package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.JwtUtils;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.service.RedisService;
import com.hrms.common.util.IpUtils;
import com.hrms.system.auth.entity.LoginLogEntity;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.service.AuthService;
import com.hrms.system.auth.service.LoginLogService;
import com.hrms.system.auth.vo.MenuVO;
import com.hrms.system.auth.vo.CurrentUserVO;
import com.hrms.system.auth.vo.LoginVO;
import com.hrms.system.auth.vo.UserInfoVO;
import com.hrms.system.auth.vo.RoleInfoVO;
import com.hrms.system.auth.entity.MenuEntity;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现（开发环境使用内存缓存，生产环境应使用 Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final MenuService menuService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogService loginLogService;
    private final RedisService redisService;

    // Redis Key 前缀
    private static final String TOKEN_BLACKLIST_KEY = "hrms:token:blacklist:";
    private static final String LOGIN_FAILED_KEY = "hrms:login:failed:";
    private static final String USER_SESSION_KEY = "hrms:session:";
    private static final int MAX_LOGIN_FAILED = 5;
    private static final long LOCK_DURATION = 30 * 60; // 30分钟（秒）

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
            // 记录登录失败日志
            recordLoginLog(null, username, 0, "用户不存在");
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 3. 校验密码（开发环境：跳过密码验证）
         //TODO: 生产环境恢复密码验证
         if (!passwordEncoder.matches(password, user.getPassword())) {
             recordLoginFailed(username);
             // 记录登录失败日志
             recordLoginLog(user.getId(), username, 0, "密码错误");
             throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "密码错误");
         }

        // 4. 清除登录失败记录
        clearLoginFailed(username);

        // 5. 查询用户角色
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        List<String> roleCodes = roleService.getRolesByUserId(user.getId())
            .stream()
            .map(RoleEntity::getRoleCode)
            .collect(Collectors.toList());

        // 6. 生成 Token（包含真实角色ID）
        String token = jwtUtils.generateToken(
            user.getId(),
            user.getUsername(),
            user.getId(), // TODO: 使用实际部门ID
            roleIds
        );

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(jwtUtils.getExpiration() / 1000);

        // 构建 UserInfoVO
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());

        // 构建角色信息列表
        List<RoleInfoVO> roleInfoList = roleService.getRolesByUserId(user.getId())
            .stream()
            .map(role -> {
                RoleInfoVO roleInfo = new RoleInfoVO();
                roleInfo.setRoleId(role.getId());
                roleInfo.setRoleName(role.getRoleName());
                roleInfo.setRoleCode(role.getRoleCode());
                roleInfo.setDataScope(role.getDataScope());
                return roleInfo;
            })
            .collect(Collectors.toList());
        userInfoVO.setRoles(roleInfoList);

        // 查询权限列表
        List<String> permissions = menuService.getPermissionsByRoleIds(roleIds);
        userInfoVO.setPermissions(permissions);

        loginVO.setUserInfo(userInfoVO);

        // 记录登录成功日志
        recordLoginLog(user.getId(), username, 1, null);

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        return loginVO;
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 将 Token 加入黑名单，过期时间与 Token 一致
            long ttl = jwtUtils.getExpiration() / 1000;
            redisService.addTokenBlacklist(token, ttl);
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查是否在黑名单中
        if (redisService.isTokenBlacklisted(token)) {
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
        currentUserVO.setRealName(user.getRealName());
        currentUserVO.setEmail(user.getEmail());
        currentUserVO.setPhone(user.getPhone());
        currentUserVO.setAvatarUrl(user.getAvatarUrl());
        currentUserVO.setStatus(user.getStatus());
        currentUserVO.setEmployeeId(user.getEmployeeId());

        // 查询用户角色
        List<RoleEntity> roles = roleService.getRolesByUserId(userId);

        // 构建角色信息列表（对象数组）
        List<RoleInfoVO> roleInfoList = roles.stream()
            .map(role -> {
                RoleInfoVO roleInfo = new RoleInfoVO();
                roleInfo.setRoleId(role.getId());
                roleInfo.setRoleName(role.getRoleName());
                roleInfo.setRoleCode(role.getRoleCode());
                roleInfo.setDataScope(role.getDataScope());
                return roleInfo;
            })
            .collect(Collectors.toList());
        currentUserVO.setRoles(roleInfoList);

        // 查询用户权限
        List<Long> roleIds = roles.stream()
            .map(RoleEntity::getId)
            .collect(Collectors.toList());
        List<String> permissions = menuService.getPermissionsByRoleIds(roleIds);
        currentUserVO.setPermissions(permissions);

        // 查询用户菜单树
        List<MenuEntity> menuEntities = menuService.getMenusByRoleIds(roleIds);
        List<MenuVO> menuVOs = menuEntities.stream()
            .map(entity -> {
                MenuVO vo = new MenuVO();
                vo.setId(entity.getId());
                vo.setName(entity.getMenuName());
                vo.setPath(entity.getPath());
                vo.setComponent(entity.getComponent());
                vo.setIcon(entity.getIcon());
                vo.setSort(entity.getSortNo());
                vo.setParentId(entity.getParentId());
                return vo;
            })
            .collect(Collectors.toList());
        currentUserVO.setMenus(menuVOs);

        return currentUserVO;
    }

    @Override
    public Integer getDataScope(Long userId) {
        // 查询用户角色
        List<RoleEntity> roles = roleService.getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return 1; // 默认仅本人
        }

        // 检查是否有超级管理员角色
        boolean isAdmin = roles.stream()
            .anyMatch(role -> "ADMIN".equals(role.getRoleCode()) || "ROLE_ADMIN".equals(role.getRoleCode()));
        if (isAdmin) {
            return 4; // 超级管理员返回全部权限
        }

        // 取最大数据权限范围
        return roles.stream()
            .mapToInt(RoleEntity::getDataScope)
            .max()
            .orElse(1);
    }

    /**
     * 检查账号是否被锁定
     */
    private void checkAccountLocked(String username) {
        long failedCount = redisService.getLoginFailedCount(username);
        if (failedCount >= MAX_LOGIN_FAILED) {
            throw new GlobalException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailed(String username) {
        redisService.recordLoginFailed(username, LOCK_DURATION);
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailed(String username) {
        redisService.clearLoginFailed(username);
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, Integer status, String errorMsg) {
        try {
            String userAgent = IpUtils.getUserAgent();
            LoginLogEntity loginLog = new LoginLogEntity();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginType("ACCOUNT");
            loginLog.setIp(IpUtils.getIpAddr());
            loginLog.setBrowser(IpUtils.getBrowser(userAgent));
            loginLog.setOs(IpUtils.getOs(userAgent));
            loginLog.setStatus(status);
            loginLog.setErrorMsg(errorMsg);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLogService.recordLoginLog(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(Long userId) {
        try {
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtils.getIpAddr());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("更新最后登录时间失败: {}", e.getMessage(), e);
        }
    }

}

package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import com.hrms.system.auth.dto.LoginRequestDTO;
import com.hrms.system.auth.entity.MenuDO;
import com.hrms.system.auth.entity.RoleDO;
import com.hrms.system.auth.entity.UserDO;
import com.hrms.system.auth.mapper.MenuMapper;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.service.AuthService;
import com.hrms.system.auth.util.JwtUtils;
import com.hrms.system.auth.util.PasswordUtils;
import com.hrms.system.auth.util.UserStatusEnum;
import com.hrms.system.auth.vo.LoginVO;
import com.hrms.system.log.entity.LoginLogDO;
import com.hrms.system.log.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现类。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final JwtUtils jwtUtils;

    @Autowired
    private LoginLogService loginLogService;

    public AuthServiceImpl(UserMapper userMapper, RoleMapper roleMapper, MenuMapper menuMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginVO login(LoginRequestDTO request) {
        // 获取请求信息用于日志记录
        HttpServletRequest httpRequest = getHttpRequest();
        String ipAddress = getIpAddress(httpRequest);
        String deviceInfo = httpRequest != null ? httpRequest.getHeader("User-Agent") : "";

        // 1. 校验参数
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            recordLoginLog(null, request.getUsername(), false, "参数错误", ipAddress, deviceInfo);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名或密码不能为空");
        }

        // 2. 查询用户
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, request.getUsername());
        UserDO user = userMapper.selectOne(wrapper);

        if (user == null) {
            recordLoginLog(null, request.getUsername(), false, "用户名不存在", ipAddress, deviceInfo);
            throw new BusinessException(ErrorCode.AUTH_ERROR, "用户名或密码错误");
        }

        // 3. 校验密码
        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            recordLoginLog(user.getId(), request.getUsername(), false, "密码错误", ipAddress, deviceInfo);
            throw new BusinessException(ErrorCode.AUTH_ERROR, "用户名或密码错误");
        }

        // 4. 校验用户状态
        if (!UserStatusEnum.isEnabled(user.getStatus())) {
            recordLoginLog(user.getId(), request.getUsername(), false, "账号已被禁用", ipAddress, deviceInfo);
            throw new BusinessException(ErrorCode.AUTH_ERROR, "账号已被禁用");
        }

        // 5. 查询用户角色
        List<RoleDO> roles = userMapper.selectRolesByUserId(user.getId());
        List<Long> roleIds = roles.stream().map(RoleDO::getId).collect(Collectors.toList());

        // 6. 查询用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        if (permissions == null) {
            permissions = Collections.emptyList();
        }

        // 7. 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleIds, permissions);

        // 8. 记录登录成功日志
        recordLoginLog(user.getId(), request.getUsername(), true, null, ipAddress, deviceInfo);

        // 9. 构建响应
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRoleIds(roleIds);
        vo.setPermissions(permissions);

        return vo;
    }

    /**
     * 记录登录日志。
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param success    是否成功
     * @param failReason 失败原因
     * @param ipAddress  IP 地址
     * @param deviceInfo 设备信息
     */
    private void recordLoginLog(Long userId, String username, boolean success, String failReason, String ipAddress, String deviceInfo) {
        LoginLogDO logDO = new LoginLogDO();
        logDO.setUserId(userId != null ? userId : 0L);
        logDO.setUsername(username);
        logDO.setSuccess(success ? 1 : 0);
        logDO.setFailReason(failReason);
        logDO.setIpAddress(ipAddress);
        logDO.setDeviceInfo(deviceInfo);
        logDO.setLoginTime(LocalDateTime.now());

        loginLogService.recordLoginLogAsync(logDO);
    }

    /**
     * 获取 HTTP 请求对象。
     *
     * @return HTTP 请求对象，可能为 null
     */
    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端 IP 地址。
     *
     * @param request HTTP 请求
     * @return IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个 IP 的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public LoginVO getCurrentUser() {
        // 1. 从 SecurityContextHolder 获取当前用户
        UserContext context = SecurityContextHolder.getContext();
        if (context == null || context.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }

        // 2. 查询用户信息
        UserDO user = userMapper.selectById(context.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_ERROR, "用户不存在");
        }

        // 3. 查询用户角色
        List<RoleDO> roles = userMapper.selectRolesByUserId(user.getId());
        List<Long> roleIds = roles.stream().map(RoleDO::getId).collect(Collectors.toList());

        // 4. 查询用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        if (permissions == null) {
            permissions = Collections.emptyList();
        }

        // 5. 构建响应
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRoleIds(roleIds);
        vo.setPermissions(permissions);

        return vo;
    }

    @Override
    public void logout() {
        // JWT 无状态，服务端不需要处理
        // 前端清除 Token 即可
    }
}
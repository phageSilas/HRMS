package com.hrms.business.mycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.mycenter.dto.LoginLogVO;
import com.hrms.business.mycenter.dto.PasswordChangeRequest;
import com.hrms.business.mycenter.dto.PhoneBindRequest;
import com.hrms.business.mycenter.service.AccountService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.LoginLogEntity;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.LoginLogMapper;
import com.hrms.system.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * 账号安全服务实现
 * <p>
 * 提供密码修改、手机号绑定/解绑、登录日志查询等账号安全相关功能。
 * 密码修改校验旧密码正确性、新密码复杂度和新旧密码一致性。
 * 手机号绑定使用密码验证身份（非短信验证码）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 密码复杂度正则：8位以上，大小写字母 + 数字 + 特殊字符至少包含 3 种
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?![a-zA-Z]+$)(?!\\d+$)(?![^a-zA-Z\\d]+$).{8,}$");

    /**
     * 修改密码
     * <p>
     * 流程：校验旧密码 → 校验新密码复杂度 → 校验新旧密码不同 → 加密更新密码。
     * 密码使用 BCrypt 加密存储。
     * </p>
     *
     * @param userId  用户 ID
     * @param request 密码修改请求（含旧密码、新密码）
     * @throws GlobalException 用户不存在、旧密码错误、密码不符合规则或新旧密码相同时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, PasswordChangeRequest request) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 1. 校验旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "旧密码错误");
        }

        // 2. 校验新密码复杂度
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED,
                    "新密码需8位以上，包含大小写字母、数字、特殊字符中至少3种");
        }

        // 3. 新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "新密码不能与旧密码相同");
        }

        // TODO: 如果启用 Redis，可校验新密码不能与最近3次使用过的密码相同

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户 {} 密码修改成功", userId);
    }

    /**
     * 绑定/更换手机号
     * <p>
     * 通过验证当前登录密码确认身份，无需短信验证码。
     * </p>
     *
     * @param userId  用户 ID
     * @param request 手机号绑定请求（含密码和新手机号）
     * @throws GlobalException 用户不存在或密码错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, PhoneBindRequest request) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 校验当前登录密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.PASSWORD_MISMATCH, "密码错误，更换手机失败");
        }

        user.setPhone(request.getPhone());
        userMapper.updateById(user);
        log.info("用户 {} 更换手机号成功 → {}", userId, request.getPhone());
    }

    /**
     * 解绑手机号
     * <p>
     * 直接清空用户绑定的手机号。当前短信服务未对接，暂不校验短信验证码。
     * </p>
     *
     * @param userId 用户 ID
     * @throws GlobalException 用户不存在或未绑定手机号时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindPhone(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.isBlank(user.getPhone())) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "未绑定手机号，无需解绑");
        }

        // 当前简化：直接清空手机号
        user.setPhone(null);
        userMapper.updateById(user);
        log.info("用户 {} 解绑手机号成功", userId);
    }

    /**
     * 查询最近 50 条登录日志
     *
     * @param userId 用户 ID
     * @return 登录日志 VO 列表
     */
    @Override
    public List<LoginLogVO> getLoginLogs(Long userId) {
        List<LoginLogEntity> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<LoginLogEntity>()
                        .eq(LoginLogEntity::getUserId, userId)
                        .orderByDesc(LoginLogEntity::getLoginTime)
                        .last("LIMIT 50")
        );

        return logs.stream().map(l -> {
            LoginLogVO vo = new LoginLogVO();
            vo.setIp(l.getIp());
            vo.setLoginLocation(l.getLoginLocation());
            vo.setBrowser(l.getBrowser());
            vo.setOs(l.getOs());
            vo.setStatus(l.getStatus());
            vo.setErrorMsg(l.getErrorMsg());
            vo.setLoginTime(l.getLoginTime());
            return vo;
        }).collect(Collectors.toList());
    }
}

package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.entity.RoleDO;
import com.hrms.system.auth.entity.UserDO;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.util.PasswordUtils;
import com.hrms.system.auth.vo.RoleVO;
import com.hrms.system.auth.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Long create(UserCreateDTO dto) {
        // 1. 校验参数
        if (dto == null || !StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }

        // 2. 校验密码
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
        }
        if (dto.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不能小于 6 位");
        }

        // 3. 校验用户名是否已存在
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        // 4. 创建用户
        UserDO user = new UserDO();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setEmployeeId(dto.getEmployeeId());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setRemark(dto.getRemark());

        userMapper.insert(user);

        return user.getId();
    }

    @Override
    public void update(UserUpdateDTO dto) {
        // 1. 校验参数
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        // 2. 查询用户
        UserDO user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        // 3. 更新用户信息
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getEmployeeId() != null) {
            user.setEmployeeId(dto.getEmployeeId());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            user.setRemark(dto.getRemark());
        }

        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        userMapper.deleteById(id);
    }

    @Override
    public UserVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        // 查询用户角色
        List<RoleDO> roles = userMapper.selectRolesByUserId(id);

        return convertToVO(user, roles);
    }

    @Override
    public Page<UserVO> list(UserQueryDTO dto) {
        if (dto == null) {
            dto = new UserQueryDTO();
        }

        // 构建查询条件
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(UserDO::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            wrapper.like(UserDO::getRealName, dto.getRealName());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(UserDO::getPhone, dto.getPhone());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(UserDO::getStatus, dto.getStatus());
        }

        // 分页查询
        Page<UserDO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<UserDO> result = userMapper.selectPage(page, wrapper);

        // 转换结果
        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserVO> voList = result.getRecords().stream()
                .map(user -> convertToVO(user, null))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不能小于 6 位");
        }

        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        user.setPassword(PasswordUtils.encode(newPassword));
        user.setNeedChangePassword(1);
        userMapper.updateById(user);
    }

    /**
     * 转换为 VO。
     */
    private UserVO convertToVO(UserDO user, List<RoleDO> roles) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setNeedChangePassword(user.getNeedChangePassword());
        vo.setRemark(user.getRemark());
        vo.setCreateTime(user.getCreateTime());

        if (roles != null && !roles.isEmpty()) {
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            vo.setRoles(roleVOs);
        }

        return vo;
    }

    /**
     * 转换为 RoleVO。
     */
    private RoleVO convertToRoleVO(RoleDO role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setSortNo(role.getSortNo());
        vo.setRemark(role.getRemark());
        return vo;
    }
}
package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.ResetPasswordVO;
import com.hrms.system.auth.vo.UserCreateResultVO;
import com.hrms.system.auth.vo.UserDetailVO;
import com.hrms.system.auth.vo.UserListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserListVO> listUsers(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, 0);

        // 关键词搜索（匹配用户名/真实姓名/手机号）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                    .or()
                    .like(UserEntity::getRealName, keyword)
                    .or()
                    .like(UserEntity::getPhone, keyword));
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, queryDTO.getStatus());
        }

        // 部门筛选（如果用户表有deptId字段）
        // TODO: 用户表目前没有deptId字段，需要在UserEntity中添加

        // 按创建时间降序
        wrapper.orderByDesc(UserEntity::getCreateTime);

        // 执行分页查询
        Page<UserEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<UserEntity> userPage = userMapper.selectPage(page, wrapper);

        // 查询所有用户的角色信息
        List<Long> userIds = userPage.getRecords().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());

        // 批量查询用户角色关联
        Map<Long, List<Long>> userRoleMap = userIds.isEmpty() ? Map.of() :
                userRoleMapper.selectList(
                                new LambdaQueryWrapper<UserRoleEntity>()
                                        .in(UserRoleEntity::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(
                                UserRoleEntity::getUserId,
                                Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())
                        ));

        // 查询所有角色信息
        List<RoleEntity> allRoles = roleService.list();
        Map<Long, String> roleNameMap = allRoles.stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getRoleName));

        // 转换为VO
        List<UserListVO> records = userPage.getRecords().stream().map(user -> {
            UserListVO vo = new UserListVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setStatus(user.getStatus());
            vo.setEmployeeId(user.getEmployeeId());
            // TODO: employeeNo 和 deptName 需要从员工模块查询
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setCreateTime(user.getCreateTime());

            // 设置角色名称列表
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), List.of());
            List<String> roleNames = roleIds.stream()
                    .map(roleNameMap::get)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            vo.setRoleNames(roleNames);

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, userPage.getTotal(), (int) userPage.getCurrent(), (int) userPage.getSize());
    }

    @Override
    public UserDetailVO getUserDetail(Long id) {
        // 查询用户
        UserEntity user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            return null;
        }

        // 查询用户角色
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRoleEntity>()
                                .eq(UserRoleEntity::getUserId, id))
                .stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());

        // 构建VO
        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRoleIds(roleIds);
        vo.setEmployeeId(user.getEmployeeId());
        // TODO: employeeNo, deptId 需要从员工模块查询
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setRemark(user.getRemark());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCreateResultVO createUser(UserCreateDTO createDTO) {
        // 1. 校验用户名唯一性
        checkUsernameUnique(createDTO.getUsername(), null);

        // 2. 校验手机号唯一性
        checkPhoneUnique(createDTO.getPhone(), null);

        // 3. 校验角色ID是否存在
        validateRoleIds(createDTO.getRoleIds());

        // 4. 创建用户实体
        UserEntity user = new UserEntity();
        user.setUsername(createDTO.getUsername());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setRealName(createDTO.getRealName());
        user.setPhone(createDTO.getPhone());
        user.setEmail(createDTO.getEmail());
        user.setStatus(1); // 默认启用
        user.setEmployeeId(createDTO.getEmployeeId());
        user.setNeedChangePassword(1); // 首次登录强制修改密码
        user.setPasswordUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 5. 关联角色
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            for (Long roleId : createDTO.getRoleIds()) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        // 6. 构建返回结果
        UserCreateResultVO result = new UserCreateResultVO();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setPhone(user.getPhone());
        result.setNeedChangePassword(true);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        // 1. 查询用户是否存在
        UserEntity user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 2. 校验手机号唯一性（如果修改了手机号）
        if (StringUtils.hasText(updateDTO.getPhone()) && !updateDTO.getPhone().equals(user.getPhone())) {
            checkPhoneUnique(updateDTO.getPhone(), id);
        }

        // 3. 校验角色ID是否存在
        if (updateDTO.getRoleIds() != null && !updateDTO.getRoleIds().isEmpty()) {
            validateRoleIds(updateDTO.getRoleIds());
        }

        // 4. 更新用户信息
        user.setRealName(updateDTO.getRealName());
        user.setPhone(updateDTO.getPhone());
        user.setEmail(updateDTO.getEmail());
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }
        // TODO: deptId 需要在UserEntity中添加
        userMapper.updateById(user);

        // 5. 更新角色关联（全量覆盖）
        if (updateDTO.getRoleIds() != null) {
            // 物理删除旧关联（使用自定义SQL或deleteById）
            userRoleMapper.deleteByUserId(id);

            // 插入新关联
            for (Long roleId : updateDTO.getRoleIds()) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 1. 查询用户是否存在
        UserEntity user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 2. 逻辑删除用户
        userMapper.deleteById(id);

        // 3. 删除角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRoleEntity>()
                        .eq(UserRoleEntity::getUserId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResetPasswordVO resetPassword(Long id) {
        // 1. 查询用户是否存在
        UserEntity user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 2. 生成随机密码
        String newPassword = generateRandomPassword();

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedChangePassword(1); // 强制下次登录修改密码
        user.setPasswordUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 4. 构建返回结果
        ResetPasswordVO result = new ResetPasswordVO();
        result.setNewPassword(newPassword);
        result.setNeedChangePassword(true);

        return result;
    }

    /**
     * 校验用户名唯一性
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        wrapper.eq(UserEntity::getIsDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(UserEntity::getId, excludeId);
        }

        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "用户名已存在");
        }
    }

    /**
     * 校验手机号唯一性
     */
    private void checkPhoneUnique(String phone, Long excludeId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone);
        wrapper.eq(UserEntity::getIsDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(UserEntity::getId, excludeId);
        }

        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "手机号已存在");
        }
    }

    /**
     * 校验角色ID是否存在
     */
    private void validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            RoleEntity role = roleService.getById(roleId);
            if (role == null) {
                throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "角色ID不存在: " + roleId);
            }
        }
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        // 生成8位随机密码，包含大小写字母、数字和特殊字符
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}

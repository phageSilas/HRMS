package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleEntity role) {
        // 校验角色编码唯一性
        checkRoleCodeUnique(role.getRoleCode(), null);

        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleEntity role) {
        RoleEntity existing = roleMapper.selectById(role.getId());
        if (existing == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // 校验角色编码唯一性（排除自身）
        checkRoleCodeUnique(role.getRoleCode(), role.getId());

        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RoleEntity existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        roleMapper.deleteById(id);
    }

    @Override
    public RoleEntity getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<RoleEntity> list() {
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getStatus, 1);
        wrapper.orderByAsc(RoleEntity::getSortNo);
        return roleMapper.selectList(wrapper);
    }

    @Override
    public Page<RoleEntity> page(Page<RoleEntity> page, String roleName, Integer status) {
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(roleName)) {
            wrapper.like(RoleEntity::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(RoleEntity::getStatus, status);
        }

        wrapper.orderByAsc(RoleEntity::getSortNo);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<RoleEntity> getRolesByUserId(Long userId) {
        // 查询用户的角色 ID 列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        // 查询角色详情
        return roleIds.stream()
            .map(roleMapper::selectById)
            .filter(role -> role != null && role.getStatus() == 1)
            .sorted(Comparator.comparing(RoleEntity::getSortNo))
            .collect(Collectors.toList());
    }

    @Override
    public Integer getDataScope(Long userId) {
        List<RoleEntity> roles = getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return 1; // 默认仅本人
        }

        // 取最大数据权限范围
        return roles.stream()
            .mapToInt(RoleEntity::getDataScope)
            .max()
            .orElse(1);
    }

    /**
     * 校验角色编码唯一性
     */
    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getRoleCode, roleCode);
        if (excludeId != null) {
            wrapper.ne(RoleEntity::getId, excludeId);
        }

        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "角色编码已存在");
        }
    }

}

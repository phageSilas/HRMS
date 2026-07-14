package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.RoleMenuEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hrms.system.auth.dto.RoleMenuAssignDTO;
import com.hrms.system.auth.mapper.RoleMenuMapper;
import com.hrms.system.auth.vo.RoleVO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final RoleMenuMapper roleMenuMapper;

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

    @Override
    public List<RoleVO> listRoleVOs() {
        // 1. 查询所有角色
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getStatus, 1);
        wrapper.orderByAsc(RoleEntity::getSortNo);
        List<RoleEntity> roles = roleMapper.selectList(wrapper);

        if (roles.isEmpty()) {
            return List.of();
        }

        // 2. 提取所有角色ID
        List<Long> roleIds = roles.stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toList());

        // 3. 批量查询角色菜单关联
        List<RoleMenuEntity> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenuEntity>()
                        .in(RoleMenuEntity::getRoleId, roleIds)
                        .eq(RoleMenuEntity::getIsDeleted, 0));

        // 4. 按角色ID分组菜单ID
        Map<Long, List<Long>> roleMenuMap = roleMenus.stream()
                .collect(Collectors.groupingBy(
                        RoleMenuEntity::getRoleId,
                        Collectors.mapping(RoleMenuEntity::getMenuId, Collectors.toList())
                ));

        // 5. 转换为VO
        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            vo.setDataScope(role.getDataScope());
            vo.setStatus(role.getStatus());
            vo.setSortNo(role.getSortNo());
            vo.setRemark(role.getRemark());
            vo.setMenuIds(roleMenuMap.getOrDefault(role.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, RoleMenuAssignDTO roleMenuAssignDTO) {
        // 1. 校验角色是否存在
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // 2. 删除该角色的所有旧菜单关联（逻辑删除）
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenuEntity>()
                        .eq(RoleMenuEntity::getRoleId, roleId));

        // 3. 批量插入新关联
        List<Long> menuIds = roleMenuAssignDTO.getMenuIds();
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenuEntity roleMenu = new RoleMenuEntity();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
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

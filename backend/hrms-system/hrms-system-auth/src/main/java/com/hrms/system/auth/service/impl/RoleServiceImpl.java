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

import com.hrms.system.auth.dto.RoleCreateDTO;
import com.hrms.system.auth.dto.RoleMenuAssignDTO;
import com.hrms.system.auth.dto.RoleUpdateDTO;
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
    public Long create(RoleCreateDTO createDTO) {
        // 校验角色编码唯一性
        checkRoleCodeUnique(createDTO.getRoleCode(), null);

        RoleEntity role = new RoleEntity();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleCode(createDTO.getRoleCode());
        role.setDataScope(createDTO.getDataScope());
        role.setSortNo(createDTO.getSortNo());
        role.setStatus(createDTO.getStatus());
        role.setRemark(createDTO.getRemark());

        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleUpdateDTO updateDTO) {
        RoleEntity existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // 校验角色编码唯一性（排除自身）
        if (StringUtils.hasText(updateDTO.getRoleCode())) {
            checkRoleCodeUnique(updateDTO.getRoleCode(), id);
        }

        existing.setRoleName(updateDTO.getRoleName());
        existing.setRoleCode(updateDTO.getRoleCode());
        existing.setDataScope(updateDTO.getDataScope());
        existing.setSortNo(updateDTO.getSortNo());
        existing.setStatus(updateDTO.getStatus());
        existing.setRemark(updateDTO.getRemark());

        roleMapper.updateById(existing);
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
    public List<RoleVO> listRoleVOs(String keyword, Integer status) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(RoleEntity::getRoleName, keyword);
        }
        if (status != null) {
            wrapper.eq(RoleEntity::getStatus, status);
        }
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

        // 2. 查询该角色所有已有的菜单关联（包括已删除的）
        List<RoleMenuEntity> existingMenus = roleMenuMapper.selectAllByRoleId(roleId);
        Map<Long, RoleMenuEntity> existingMenuMap = existingMenus.stream()
                .collect(Collectors.toMap(RoleMenuEntity::getMenuId, rm -> rm));

        List<Long> newMenuIds = roleMenuAssignDTO.getMenuIds();
        if (newMenuIds == null) {
            newMenuIds = List.of();
        }

        // 获取当前操作用户ID
        Long currentUserId = com.hrms.common.security.SecurityContextHolder.getUserId();

        // 3. 处理新分配的菜单
        for (Long menuId : newMenuIds) {
            RoleMenuEntity existing = existingMenuMap.get(menuId);
            if (existing != null) {
                // 如果已存在但已删除，则恢复（使用自定义SQL绕过逻辑删除）
                if (existing.getIsDeleted() == 1) {
                    roleMenuMapper.restoreById(existing.getId(), currentUserId);
                }
            } else {
                // 如果不存在，则插入新关联
                RoleMenuEntity roleMenu = new RoleMenuEntity();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        // 4. 处理需要移除的菜单（逻辑删除）
        for (RoleMenuEntity existing : existingMenus) {
            if (existing.getIsDeleted() == 0 && !newMenuIds.contains(existing.getMenuId())) {
                // 当前存在但新列表中没有，逻辑删除
                roleMenuMapper.deleteById(existing.getId());
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

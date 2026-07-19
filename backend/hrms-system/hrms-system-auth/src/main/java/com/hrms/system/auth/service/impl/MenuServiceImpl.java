package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.MenuEntity;
import com.hrms.system.auth.mapper.MenuMapper;
import com.hrms.system.auth.mapper.RoleMenuMapper;
import com.hrms.system.auth.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MenuEntity menu) {
        // 校验父菜单是否存在
        if (menu.getParentId() != null && menu.getParentId() > 0) {
            MenuEntity parent = menuMapper.selectById(menu.getParentId());
            if (parent == null) {
                throw new GlobalException(ErrorCode.NOT_FOUND, "父菜单不存在");
            }
        } else {
            menu.setParentId(0L);
        }

        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuEntity menu) {
        MenuEntity existing = menuMapper.selectById(menu.getId());
        if (existing == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "菜单不存在");
        }

        // 校验不能将自己设为父菜单
        if (menu.getParentId() != null && menu.getParentId().equals(menu.getId())) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "不能将自己设为父菜单");
        }

        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MenuEntity existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "菜单不存在");
        }

        // 检查是否有子菜单
        LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuEntity::getParentId, id);
        Long count = menuMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "该菜单下有子菜单，不能删除");
        }

        menuMapper.deleteById(id);
    }

    @Override
    public MenuEntity getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public List<MenuEntity> list(String keyword, Integer status) {
        LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MenuEntity::getMenuName, keyword);
        }
        if (status != null) {
            wrapper.eq(MenuEntity::getStatus, status);
        }
        wrapper.orderByAsc(MenuEntity::getSortNo);
        return menuMapper.selectList(wrapper);
    }

    @Override
    public Page<MenuEntity> page(Page<MenuEntity> page, String menuName, Integer status) {
        LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(menuName)) {
            wrapper.like(MenuEntity::getMenuName, menuName);
        }
        if (status != null) {
            wrapper.eq(MenuEntity::getStatus, status);
        }

        wrapper.orderByAsc(MenuEntity::getSortNo);
        return menuMapper.selectPage(page, wrapper);
    }

    @Override
    public List<MenuEntity> buildMenuTree() {
        // 查询所有菜单（用于权限分配，不过滤 visible）
        // visible 字段只影响用户侧边栏显示，不应影响权限分配
        LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuEntity::getStatus, 1);
        wrapper.orderByAsc(MenuEntity::getSortNo);
        List<MenuEntity> allMenus = menuMapper.selectList(wrapper);

        return buildTree(allMenus);
    }

    @Override
    public List<MenuEntity> getMenusByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectMenusByRoleIds(roleIds);
    }

    @Override
    public List<String> getPermissionsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        List<MenuEntity> menus = menuMapper.selectMenusByRoleIds(roleIds);
        return menus.stream()
            .map(MenuEntity::getPermission)
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 构建菜单树
     */
    private List<MenuEntity> buildTree(List<MenuEntity> allMenus) {
        // 按 ID 分组
        Map<Long, MenuEntity> menuMap = allMenus.stream()
            .collect(Collectors.toMap(MenuEntity::getId, menu -> menu));

        // 构建树形结构
        List<MenuEntity> rootMenus = new ArrayList<>();

        for (MenuEntity menu : allMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
            } else {
                MenuEntity parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }

        // 按 sortNo 排序根节点
        rootMenus.sort(Comparator.comparingInt(MenuEntity::getSortNo));

        return rootMenus;
    }

}

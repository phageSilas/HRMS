package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.system.auth.dto.MenuCreateDTO;
import com.hrms.system.auth.dto.MenuQueryDTO;
import com.hrms.system.auth.dto.MenuUpdateDTO;
import com.hrms.system.auth.entity.MenuDO;
import com.hrms.system.auth.mapper.MenuMapper;
import com.hrms.system.auth.service.MenuService;
import com.hrms.system.auth.convert.MenuConvert;
import com.hrms.system.auth.vo.MenuTreeVO;
import com.hrms.system.auth.vo.MenuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    @Transactional
    public Long create(MenuCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getMenuName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单名称不能为空");
        }

        if (dto.getMenuType() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单类型不能为空");
        }

        // 校验父菜单
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            MenuDO parent = menuMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "父菜单不存在");
            }
        }

        MenuDO menu = MenuConvert.toDO(dto);

        // 设置默认值
        if (menu.getParentId() == null) {
            menu.setParentId(0L); // 根菜单
        }
        if (menu.getSortNo() == null) {
            menu.setSortNo(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }

        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional
    public void update(MenuUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单 ID 不能为空");
        }

        MenuDO menu = menuMapper.selectById(dto.getId());
        if (menu == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单不存在");
        }

        // 不能将自己设为自己的父菜单
        if (dto.getParentId() != null && dto.getParentId().equals(dto.getId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "父菜单不能为自己");
        }

        MenuDO updateMenu = MenuConvert.toDO(dto);
        menuMapper.updateById(updateMenu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单 ID 不能为空");
        }

        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单不存在");
        }

        // 查询子菜单
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getParentId, id);
        List<MenuDO> children = menuMapper.selectList(wrapper);

        // 递归删除子菜单
        for (MenuDO child : children) {
            delete(child.getId());
        }

        // 删除菜单
        menuMapper.deleteById(id);
    }

    @Override
    public MenuVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单 ID 不能为空");
        }

        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "菜单不存在");
        }

        return MenuConvert.toVO(menu);
    }

    @Override
    public List<MenuVO> list(MenuQueryDTO dto) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getMenuName())) {
                wrapper.like(MenuDO::getMenuName, dto.getMenuName());
            }
            if (dto.getMenuType() != null) {
                wrapper.eq(MenuDO::getMenuType, dto.getMenuType());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(MenuDO::getStatus, dto.getStatus());
            }
        }
        wrapper.orderByAsc(MenuDO::getSortNo);

        List<MenuDO> menus = menuMapper.selectList(wrapper);
        return MenuConvert.toVOList(menus);
    }

    @Override
    public List<MenuTreeVO> tree() {
        // 查询所有菜单
        List<MenuDO> allMenus = menuMapper.selectList(null);

        // 构建树形结构
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<MenuTreeVO> getUserMenuTree() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 查询用户有权限的菜单 ID 列表
        List<Long> menuIds = menuMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询菜单详情
        List<MenuDO> menus = menuMapper.selectBatchIds(menuIds);

        // 构建树形结构
        return buildTree(menus, 0L);
    }

    /**
     * 构建菜单树。
     *
     * @param allMenus 所有菜单
     * @param parentId 父菜单 ID
     * @return 菜单树
     */
    private List<MenuTreeVO> buildTree(List<MenuDO> allMenus, Long parentId) {
        if (allMenus == null || allMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 parentId 分组
        Map<Long, List<MenuDO>> menuMap = allMenus.stream()
                .collect(Collectors.groupingBy(MenuDO::getParentId));

        // 递归构建树
        return buildTreeRecursive(menuMap, parentId);
    }

    /**
     * 递归构建菜单树。
     *
     * @param menuMap  菜单分组 Map
     * @param parentId 父菜单 ID
     * @return 菜单树
     */
    private List<MenuTreeVO> buildTreeRecursive(Map<Long, List<MenuDO>> menuMap, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();
        List<MenuDO> children = menuMap.get(parentId);

        if (children != null) {
            for (MenuDO menu : children) {
                MenuTreeVO node = MenuConvert.toTreeVO(menu);

                // 递归查询子菜单
                List<MenuTreeVO> subChildren = buildTreeRecursive(menuMap, menu.getId());
                if (!subChildren.isEmpty()) {
                    node.setChildren(subChildren);
                }

                tree.add(node);
            }
        }

        return tree;
    }
}
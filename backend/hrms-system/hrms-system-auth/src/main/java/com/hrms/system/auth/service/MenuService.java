package com.hrms.system.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.auth.entity.MenuEntity;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 创建菜单
     *
     * @param menu 菜单实体
     * @return 菜单 ID
     */
    Long create(MenuEntity menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     */
    void update(MenuEntity menu);

    /**
     * 删除菜单
     *
     * @param id 菜单 ID
     */
    void delete(Long id);

    /**
     * 根据 ID 查询菜单
     *
     * @param id 菜单 ID
     * @return 菜单实体
     */
    MenuEntity getById(Long id);

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    List<MenuEntity> list();

    /**
     * 分页查询菜单
     *
     * @param page     分页参数
     * @param menuName 菜单名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    Page<MenuEntity> page(Page<MenuEntity> page, String menuName, Integer status);

    /**
     * 构建菜单树
     *
     * @return 菜单树列表
     */
    List<MenuEntity> buildMenuTree();

    /**
     * 根据角色 ID 列表查询菜单列表
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单列表
     */
    List<MenuEntity> getMenusByRoleIds(List<Long> roleIds);

    /**
     * 根据角色 ID 列表查询权限标识列表
     *
     * @param roleIds 角色 ID 列表
     * @return 权限标识列表
     */
    List<String> getPermissionsByRoleIds(List<Long> roleIds);

}

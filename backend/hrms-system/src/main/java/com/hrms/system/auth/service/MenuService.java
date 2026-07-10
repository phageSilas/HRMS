package com.hrms.system.auth.service;

import com.hrms.system.auth.dto.MenuCreateDTO;
import com.hrms.system.auth.dto.MenuQueryDTO;
import com.hrms.system.auth.dto.MenuUpdateDTO;
import com.hrms.system.auth.vo.MenuTreeVO;
import com.hrms.system.auth.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口。
 */
public interface MenuService {

    /**
     * 创建菜单。
     *
     * @param dto 菜单创建请求
     * @return 菜单 ID
     */
    Long create(MenuCreateDTO dto);

    /**
     * 更新菜单。
     *
     * @param dto 菜单更新请求
     */
    void update(MenuUpdateDTO dto);

    /**
     * 删除菜单（级联删除子菜单）。
     *
     * @param id 菜单 ID
     */
    void delete(Long id);

    /**
     * 查询菜单详情。
     *
     * @param id 菜单 ID
     * @return 菜单详情
     */
    MenuVO getById(Long id);

    /**
     * 查询菜单列表。
     *
     * @param dto 查询条件
     * @return 菜单列表
     */
    List<MenuVO> list(MenuQueryDTO dto);

    /**
     * 查询菜单树。
     *
     * @return 菜单树形结构
     */
    List<MenuTreeVO> tree();

    /**
     * 获取当前用户的菜单树。
     *
     * @return 用户有权访问的菜单树
     */
    List<MenuTreeVO> getUserMenuTree();
}
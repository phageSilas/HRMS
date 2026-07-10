package com.hrms.system.auth.convert;

import com.hrms.system.auth.dto.MenuCreateDTO;
import com.hrms.system.auth.dto.MenuUpdateDTO;
import com.hrms.system.auth.entity.MenuDO;
import com.hrms.system.auth.vo.MenuTreeVO;
import com.hrms.system.auth.vo.MenuVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单对象转换器。
 */
public class MenuConvert {

    /**
     * MenuCreateDTO 转 MenuDO。
     */
    public static MenuDO toDO(MenuCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        MenuDO entity = new MenuDO();
        entity.setParentId(dto.getParentId());
        entity.setMenuName(dto.getMenuName());
        entity.setMenuType(dto.getMenuType());
        entity.setPath(dto.getPath());
        entity.setComponent(dto.getComponent());
        entity.setPermission(dto.getPermission());
        entity.setIcon(dto.getIcon());
        entity.setSortNo(dto.getSortNo());
        entity.setVisible(dto.getVisible());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * MenuUpdateDTO 转 MenuDO。
     */
    public static MenuDO toDO(MenuUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        MenuDO entity = new MenuDO();
        entity.setId(dto.getId());
        entity.setParentId(dto.getParentId());
        entity.setMenuName(dto.getMenuName());
        entity.setMenuType(dto.getMenuType());
        entity.setPath(dto.getPath());
        entity.setComponent(dto.getComponent());
        entity.setPermission(dto.getPermission());
        entity.setIcon(dto.getIcon());
        entity.setSortNo(dto.getSortNo());
        entity.setVisible(dto.getVisible());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * MenuDO 转 MenuVO。
     */
    public static MenuVO toVO(MenuDO entity) {
        if (entity == null) {
            return null;
        }
        MenuVO vo = new MenuVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setPath(entity.getPath());
        vo.setComponent(entity.getComponent());
        vo.setPermission(entity.getPermission());
        vo.setIcon(entity.getIcon());
        vo.setSortNo(entity.getSortNo());
        vo.setVisible(entity.getVisible());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * MenuDO 列表转 MenuVO 列表。
     */
    public static List<MenuVO> toVOList(List<MenuDO> entities) {
        if (entities == null) {
            return null;
        }
        List<MenuVO> vos = new ArrayList<>(entities.size());
        for (MenuDO entity : entities) {
            vos.add(toVO(entity));
        }
        return vos;
    }

    /**
     * MenuDO 转 MenuTreeVO。
     */
    public static MenuTreeVO toTreeVO(MenuDO entity) {
        if (entity == null) {
            return null;
        }
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setPath(entity.getPath());
        vo.setComponent(entity.getComponent());
        vo.setPermission(entity.getPermission());
        vo.setIcon(entity.getIcon());
        vo.setSortNo(entity.getSortNo());
        vo.setVisible(entity.getVisible());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
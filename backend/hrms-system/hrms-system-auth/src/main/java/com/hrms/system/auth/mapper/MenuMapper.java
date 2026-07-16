package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuEntity> {

    /**
     * 根据角色 ID 列表查询菜单列表
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单列表
     */
    List<MenuEntity> selectMenusByRoleIds(List<Long> roleIds);

}

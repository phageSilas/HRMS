package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper 接口。
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuDO> {

    /**
     * 按用户 ID 查询菜单 ID 列表。
     *
     * @param userId 用户 ID
     * @return 菜单 ID 列表
     */
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 按用户 ID 查询权限标识列表。
     *
     * @param userId 用户 ID
     * @return 权限标识列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
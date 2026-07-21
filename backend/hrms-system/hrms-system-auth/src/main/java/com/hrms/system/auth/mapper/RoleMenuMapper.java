package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.RoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 角色菜单关联 Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuEntity> {

    /**
     * 根据角色 ID 列表查询菜单 ID 列表
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单 ID 列表
     */
    List<Long> selectMenuIdsByRoleIds(List<Long> roleIds);

    /**
     * 查询角色的所有菜单关联（包括已删除的）
     *
     * @param roleId 角色 ID
     * @return 角色菜单关联列表
     */
    List<RoleMenuEntity> selectAllByRoleId(Long roleId);

    /**
     * 恢复已删除的角色菜单关联（绕过逻辑删除）
     *
     * @param id 记录 ID
     * @return 影响行数
     */
    @Update("UPDATE sys_role_menu SET is_deleted = 0, update_by = #{updateBy}, update_time = NOW(), version = version + 1 WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updateBy") Long updateBy);

}

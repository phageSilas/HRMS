package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    /**
     * 根据用户 ID 查询角色 ID 列表
     *
     * @param userId 用户 ID
     * @return 角色 ID 列表
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 根据用户 ID 物理删除角色关联
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);

}

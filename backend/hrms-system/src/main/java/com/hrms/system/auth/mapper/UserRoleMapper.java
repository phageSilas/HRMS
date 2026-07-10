package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.UserRoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper 接口。
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
}
package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.RoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper 接口。
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
}
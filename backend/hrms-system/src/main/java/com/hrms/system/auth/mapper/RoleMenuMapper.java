package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper 接口。
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {
}
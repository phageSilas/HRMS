package com.hrms.system.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.auth.entity.FieldPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字段权限 Mapper
 */
@Mapper
public interface FieldPermissionMapper extends BaseMapper<FieldPermissionEntity> {

    /**
     * 根据业务类型和角色ID列表查询字段权限
     *
     * @param bizType 业务类型
     * @param roleIds 角色ID列表
     * @return 字段权限列表
     */
    @Select("SELECT * FROM sys_field_permission WHERE biz_type = #{bizType} AND role_id IN (${roleIds})")
    List<FieldPermissionEntity> selectByBizTypeAndRoleIds(@Param("bizType") String bizType, @Param("roleIds") String roleIds);

}
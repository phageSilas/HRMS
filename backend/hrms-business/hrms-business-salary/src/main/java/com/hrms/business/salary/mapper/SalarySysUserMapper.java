package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.salary.entity.SalarySysUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户只读 Mapper，后续替换为 auth 模块二次验证接口。
 */
@Mapper
public interface SalarySysUserMapper extends BaseMapper<SalarySysUserEntity> {
}

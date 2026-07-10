package com.hrms.system.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.log.entity.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供基础的 CRUD 操作。</p>
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogDO> {

    // 基础 CRUD 由 BaseMapper 提供
    // 如需自定义查询，可在对应的 XML 文件中定义
}
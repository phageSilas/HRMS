package com.hrms.system.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.log.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLogEntity> {

}
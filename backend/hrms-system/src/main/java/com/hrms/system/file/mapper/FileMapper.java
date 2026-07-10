package com.hrms.system.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.file.entity.FileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件 Mapper 接口。
 */
@Mapper
public interface FileMapper extends BaseMapper<FileDO> {
}
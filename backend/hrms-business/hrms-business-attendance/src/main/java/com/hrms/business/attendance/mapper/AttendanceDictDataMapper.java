package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.DictDataEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典数据只读 Mapper（考勤模块专用）。
 */
@Mapper
public interface AttendanceDictDataMapper extends BaseMapper<DictDataEntity> {
}

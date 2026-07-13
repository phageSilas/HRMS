package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤记录 Mapper
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecordEntity> {

}

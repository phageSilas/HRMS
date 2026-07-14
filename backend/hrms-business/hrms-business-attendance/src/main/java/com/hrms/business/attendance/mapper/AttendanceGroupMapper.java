package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组 Mapper，负责 hr_attendance_group 表的基础 CRUD。
 */
@Mapper
public interface AttendanceGroupMapper extends BaseMapper<AttendanceGroupEntity> {
}

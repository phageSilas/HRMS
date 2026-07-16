package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.AttendanceGroupMemberEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组成员关系 Mapper。
 */
@Mapper
public interface AttendanceGroupMemberMapper extends BaseMapper<AttendanceGroupMemberEntity> {
}

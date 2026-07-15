package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.AttendanceCorrectionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 补卡申请 Mapper。
 */
@Mapper
public interface AttendanceCorrectionMapper extends BaseMapper<AttendanceCorrectionEntity> {
}

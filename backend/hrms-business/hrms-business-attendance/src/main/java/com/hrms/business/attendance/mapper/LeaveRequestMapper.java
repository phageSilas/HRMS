package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.LeaveRequestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假申请 Mapper。
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequestEntity> {
}

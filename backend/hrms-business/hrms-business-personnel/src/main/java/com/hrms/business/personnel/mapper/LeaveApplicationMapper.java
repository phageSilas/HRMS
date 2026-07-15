package com.hrms.business.personnel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 离职申请 Mapper
 */
@Mapper
public interface LeaveApplicationMapper extends BaseMapper<LeaveApplicationEntity> {
}

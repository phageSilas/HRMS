package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.EmployeeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工快照只读 Mapper，仅用于考勤模块读取员工基础信息。
 */
@Mapper
public interface AttendanceEmployeeSnapshotMapper extends BaseMapper<EmployeeSnapshotEntity> {
}

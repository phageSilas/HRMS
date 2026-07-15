package com.hrms.business.personnel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工快照 Mapper，仅用于只读查询员工基础信息。
 */
@Mapper
public interface EmployeeSnapshotMapper extends BaseMapper<EmployeeSnapshotEntity> {
}

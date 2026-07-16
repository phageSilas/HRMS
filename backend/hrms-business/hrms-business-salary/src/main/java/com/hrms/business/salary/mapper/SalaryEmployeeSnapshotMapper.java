package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工快照只读 Mapper，后续替换为 hrms-business-employee 公开查询接口。
 */
@Mapper
public interface SalaryEmployeeSnapshotMapper extends BaseMapper<SalaryEmployeeSnapshotEntity> {
}

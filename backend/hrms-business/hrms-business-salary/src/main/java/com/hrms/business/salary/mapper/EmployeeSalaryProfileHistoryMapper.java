package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.salary.entity.EmployeeSalaryProfileHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工薪资档案变更历史 Mapper。
 */
@Mapper
public interface EmployeeSalaryProfileHistoryMapper extends BaseMapper<EmployeeSalaryProfileHistoryEntity> {
}

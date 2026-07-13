package com.hrms.business.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.employee.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<EmployeeEntity> {

}

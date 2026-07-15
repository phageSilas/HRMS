package com.hrms.business.employee.convert;

import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 员工转换器
 */
@Mapper
public interface EmployeeConvert {

    EmployeeConvert INSTANCE = Mappers.getMapper(EmployeeConvert.class);

    /**
     * DTO 转 Entity
     */
    EmployeeEntity toEntity(EmployeeCreateDTO dto);

}

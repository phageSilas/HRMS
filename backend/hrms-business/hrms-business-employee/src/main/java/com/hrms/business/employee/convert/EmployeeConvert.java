package com.hrms.business.employee.convert;

import com.hrms.business.employee.dto.EmployeeRequestDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.vo.EmployeeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 员工转换器
 */
@Mapper
public interface EmployeeConvert {

    EmployeeConvert INSTANCE = Mappers.getMapper(EmployeeConvert.class);

    EmployeeEntity toEntity(EmployeeRequestDTO dto);

    EmployeeVO toVO(EmployeeEntity entity);

}

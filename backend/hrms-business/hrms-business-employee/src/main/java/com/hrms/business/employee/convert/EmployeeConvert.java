package com.hrms.business.employee.convert;

import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.dto.EmployeeUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.vo.EmployeeDetailVO;
import com.hrms.business.employee.vo.EmployeeListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    /**
     * Entity 转列表 VO
     */
    @Mapping(target = "genderDesc", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "postName", ignore = true)
    @Mapping(target = "employmentStatusDesc", ignore = true)
    @Mapping(target = "leaderName", ignore = true)
    EmployeeListVO toListVO(EmployeeEntity entity);

    /**
     * Entity 转详情 VO
     */
    @Mapping(target = "genderDesc", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "postName", ignore = true)
    @Mapping(target = "leaderName", ignore = true)
    @Mapping(target = "hireTypeDesc", ignore = true)
    @Mapping(target = "employmentStatusDesc", ignore = true)
    @Mapping(target = "contractTypeDesc", ignore = true)
    @Mapping(target = "fieldPermissions", ignore = true)
    EmployeeDetailVO toDetailVO(EmployeeEntity entity);

}

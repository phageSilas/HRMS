package com.hrms.business.employee.convert;

import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.dto.EmployeeUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.vo.EmployeeDetailVO;
import com.hrms.business.employee.vo.EmployeeListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 员工转换器（MapStruct）
 * <p>
 * 自动生成实现类，用于 Entity / DTO / VO 之间的转换
 * </p>
 */
@Mapper
public interface EmployeeConvert {

    EmployeeConvert INSTANCE = Mappers.getMapper(EmployeeConvert.class);

    /**
     * 创建 DTO 转 Entity
     */
    EmployeeEntity toEntity(EmployeeCreateDTO dto);

    /**
     * 更新 DTO 转 Entity（用于全量更新）
     * <p>注意：不会覆盖 id、createBy、createTime 等审计字段</p>
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createBy", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateBy", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    EmployeeEntity updateEntity(EmployeeUpdateDTO dto);

    /**
     * Entity 转列表 VO
     */
    @Mappings({
            @Mapping(target = "genderDesc", expression = "java(getGenderDesc(entity.getGender()))"),
            @Mapping(target = "employmentStatusDesc", expression = "java(getEmploymentStatusDesc(entity.getEmploymentStatus()))"),
            @Mapping(target = "deptName", ignore = true),
            @Mapping(target = "postName", ignore = true),
            @Mapping(target = "leaderName", ignore = true)
    })
    EmployeeListVO toListVO(EmployeeEntity entity);

    /**
     * Entity 转详情 VO
     */
    @Mappings({
            @Mapping(target = "genderDesc", expression = "java(getGenderDesc(entity.getGender()))"),
            @Mapping(target = "employmentStatusDesc", expression = "java(getEmploymentStatusDesc(entity.getEmploymentStatus()))"),
            @Mapping(target = "hireTypeDesc", expression = "java(getHireTypeDesc(entity.getHireType()))"),
            @Mapping(target = "contractTypeDesc", expression = "java(getContractTypeDesc(entity.getContractType()))"),
            @Mapping(target = "deptName", ignore = true),
            @Mapping(target = "postName", ignore = true),
            @Mapping(target = "leaderName", ignore = true),
            @Mapping(target = "fieldPermissions", ignore = true)
    })
    EmployeeDetailVO toDetailVO(EmployeeEntity entity);

    // ==================== 辅助方法 ====================

    default String getGenderDesc(Integer gender) {
        if (gender == null) return null;
        return gender == 1 ? "男" : (gender == 2 ? "女" : "未知");
    }

    default String getEmploymentStatusDesc(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 1 -> "试用期";
            case 2 -> "正式";
            case 3 -> "待离职";
            case 4 -> "已离职";
            default -> "未知";
        };
    }

    default String getHireTypeDesc(Integer hireType) {
        if (hireType == null) return null;
        return switch (hireType) {
            case 1 -> "全职";
            case 2 -> "兼职";
            case 3 -> "实习";
            default -> "未知";
        };
    }

    default String getContractTypeDesc(Integer contractType) {
        if (contractType == null) return null;
        return switch (contractType) {
            case 1 -> "固定期限";
            case 2 -> "无固定期限";
            case 3 -> "劳务合同";
            default -> "未知";
        };
    }

}

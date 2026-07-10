package com.hrms.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.organization.entity.DictTypeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 字典类型 Mapper 接口。
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictTypeDO> {

    /**
     * 按类型编码查询。
     */
    DictTypeDO selectByTypeCode(@Param("typeCode") String typeCode);
}
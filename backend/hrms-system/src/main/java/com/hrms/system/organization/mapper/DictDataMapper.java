package com.hrms.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.organization.entity.DictDataDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据 Mapper 接口。
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictDataDO> {

    /**
     * 按类型编码查询字典数据列表。
     */
    List<DictDataDO> selectByTypeCode(@Param("typeCode") String typeCode);
}
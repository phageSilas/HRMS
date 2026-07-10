package com.hrms.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.organization.entity.PostDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 职位 Mapper 接口。
 */
@Mapper
public interface PostMapper extends BaseMapper<PostDO> {

    /**
     * 按职位编码查询。
     *
     * @param postCode 职位编码
     * @return 职位实体
     */
    PostDO selectByPostCode(@Param("postCode") String postCode);
}
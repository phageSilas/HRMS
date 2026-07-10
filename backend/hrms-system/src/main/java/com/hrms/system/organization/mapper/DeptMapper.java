package com.hrms.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.system.organization.entity.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper 接口。
 */
@Mapper
public interface DeptMapper extends BaseMapper<DeptDO> {

    /**
     * 按祖先路径查询子部门。
     *
     * @param ancestors 祖先路径（如：0,1,2）
     * @return 子部门列表
     */
    List<DeptDO> selectChildrenByAncestors(@Param("ancestors") String ancestors);

    /**
     * 按部门编码查询。
     *
     * @param deptCode 部门编码
     * @return 部门实体
     */
    DeptDO selectByDeptCode(@Param("deptCode") String deptCode);
}
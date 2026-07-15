package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.salary.entity.SalaryBatchEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪资核算批次 Mapper。
 */
@Mapper
public interface SalaryBatchMapper extends BaseMapper<SalaryBatchEntity> {
}

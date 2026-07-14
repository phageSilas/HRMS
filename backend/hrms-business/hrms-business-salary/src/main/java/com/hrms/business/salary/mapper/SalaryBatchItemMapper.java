package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 薪资核算明细 Mapper。
 */
@Mapper
public interface SalaryBatchItemMapper extends BaseMapper<SalaryBatchItemEntity> {
}

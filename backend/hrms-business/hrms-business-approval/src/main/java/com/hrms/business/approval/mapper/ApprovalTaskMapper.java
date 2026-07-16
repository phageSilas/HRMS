package com.hrms.business.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.approval.entity.ApprovalTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批任务 Mapper
 */
@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTaskEntity> {

}

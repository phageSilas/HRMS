package com.hrms.business.mycenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.mycenter.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {

}

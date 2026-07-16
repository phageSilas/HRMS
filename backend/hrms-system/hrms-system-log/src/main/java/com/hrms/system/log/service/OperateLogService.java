package com.hrms.system.log.service;

import com.hrms.system.log.entity.OperateLogEntity;

/**
 * 操作日志服务接口
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param operateLog 操作日志实体
     */
    void recordOperateLog(OperateLogEntity operateLog);

}
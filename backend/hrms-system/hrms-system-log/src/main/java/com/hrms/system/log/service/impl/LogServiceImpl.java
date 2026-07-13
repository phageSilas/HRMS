package com.hrms.system.log.service.impl;

import com.hrms.system.log.service.LogService;
import org.springframework.stereotype.Service;

/**
 * 日志审计服务实现
 */
@Service
public class LogServiceImpl implements LogService {

    @Override
    public void recordOperation(String operation, Long userId) {
        // TODO: 实现操作日志记录逻辑
    }

    @Override
    public String queryOperationLog(Long userId) {
        // TODO: 实现操作日志查询逻辑
        return null;
    }

}

package com.hrms.system.log.service.impl;

import com.hrms.system.log.entity.OperateLogEntity;
import com.hrms.system.log.mapper.OperateLogMapper;
import com.hrms.system.log.service.OperateLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperateLogServiceImpl implements OperateLogService {

    private final OperateLogMapper operateLogMapper;

    @Override
    public void recordOperateLog(OperateLogEntity operateLog) {
        try {
            operateLogMapper.insert(operateLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

}
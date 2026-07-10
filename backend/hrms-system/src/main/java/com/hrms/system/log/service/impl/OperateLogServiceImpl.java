package com.hrms.system.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.log.dto.OperateLogQueryDTO;
import com.hrms.system.log.entity.OperateLogDO;
import com.hrms.system.log.mapper.OperateLogMapper;
import com.hrms.system.log.service.OperateLogService;
import com.hrms.system.log.vo.OperateLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现。
 */
@Service
public class OperateLogServiceImpl implements OperateLogService {

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Override
    @Async("logTaskExecutor")
    public void recordAsync(OperateLogDO logDO) {
        record(logDO);
    }

    @Override
    public void record(OperateLogDO logDO) {
        operateLogMapper.insert(logDO);
    }

    @Override
    public Page<OperateLogVO> list(OperateLogQueryDTO query) {
        Page<OperateLogDO> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<OperateLogDO> wrapper = new LambdaQueryWrapper<>();

        // 模块
        if (StringUtils.hasText(query.getModule())) {
            wrapper.like(OperateLogDO::getModule, query.getModule());
        }

        // 操作类型
        if (StringUtils.hasText(query.getAction())) {
            wrapper.like(OperateLogDO::getAction, query.getAction());
        }

        // 操作人
        if (StringUtils.hasText(query.getOperatorName())) {
            wrapper.like(OperateLogDO::getOperatorName, query.getOperatorName());
        }

        // 操作结果
        if (query.getSuccess() != null) {
            wrapper.eq(OperateLogDO::getSuccess, query.getSuccess());
        }

        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.ge(OperateLogDO::getOperateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OperateLogDO::getOperateTime, query.getEndTime());
        }

        // 按操作时间倒序
        wrapper.orderByDesc(OperateLogDO::getOperateTime);

        Page<OperateLogDO> result = operateLogMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<OperateLogVO> voPage = new Page<>();
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());

        return voPage;
    }

    /**
     * 转换为 VO。
     *
     * @param logDO 日志实体
     * @return VO
     */
    private OperateLogVO toVO(OperateLogDO logDO) {
        OperateLogVO vo = new OperateLogVO();
        vo.setId(logDO.getId());
        vo.setModule(logDO.getModule());
        vo.setAction(logDO.getAction());
        vo.setOperatorId(logDO.getOperatorId());
        vo.setOperatorName(logDO.getOperatorName());
        vo.setRequestUrl(logDO.getRequestUrl());
        vo.setRequestMethod(logDO.getRequestMethod());
        vo.setDuration(logDO.getDuration());
        vo.setSuccess(logDO.getSuccess());
        vo.setErrorMessage(logDO.getErrorMessage());
        vo.setIpAddress(logDO.getIpAddress());
        vo.setLocation(logDO.getLocation());
        vo.setOperateTime(logDO.getOperateTime());
        return vo;
    }
}
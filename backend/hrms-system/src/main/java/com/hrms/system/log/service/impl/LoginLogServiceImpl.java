package com.hrms.system.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.log.dto.LoginLogQueryDTO;
import com.hrms.system.log.entity.LoginLogDO;
import com.hrms.system.log.mapper.LoginLogMapper;
import com.hrms.system.log.service.LoginLogService;
import com.hrms.system.log.vo.LoginLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录日志服务实现。
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public void recordLoginLog(LoginLogDO logDO) {
        loginLogMapper.insert(logDO);
    }

    @Override
    @Async("logTaskExecutor")
    public void recordLoginLogAsync(LoginLogDO logDO) {
        recordLoginLog(logDO);
    }

    @Override
    public Page<LoginLogVO> list(LoginLogQueryDTO query) {
        Page<LoginLogDO> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<LoginLogDO> wrapper = new LambdaQueryWrapper<>();

        // 用户名
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(LoginLogDO::getUsername, query.getUsername());
        }

        // 登录结果
        if (query.getSuccess() != null) {
            wrapper.eq(LoginLogDO::getSuccess, query.getSuccess());
        }

        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.ge(LoginLogDO::getLoginTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(LoginLogDO::getLoginTime, query.getEndTime());
        }

        // 按登录时间倒序
        wrapper.orderByDesc(LoginLogDO::getLoginTime);

        Page<LoginLogDO> result = loginLogMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<LoginLogVO> voPage = new Page<>();
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
    private LoginLogVO toVO(LoginLogDO logDO) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(logDO.getId());
        vo.setUserId(logDO.getUserId());
        vo.setUsername(logDO.getUsername());
        vo.setSuccess(logDO.getSuccess());
        vo.setIpAddress(logDO.getIpAddress());
        vo.setLocation(logDO.getLocation());
        vo.setDeviceInfo(logDO.getDeviceInfo());
        vo.setLoginTime(logDO.getLoginTime());
        vo.setFailReason(logDO.getFailReason());
        return vo;
    }
}
package com.hrms.system.auth.service;

import com.hrms.system.auth.entity.LoginLogEntity;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {

    /**
     * 记录登录日志
     *
     * @param loginLog 登录日志实体
     */
    void recordLoginLog(LoginLogEntity loginLog);

}
package com.hrms.system.log.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.log.dto.LoginLogQueryDTO;
import com.hrms.system.log.entity.LoginLogDO;
import com.hrms.system.log.vo.LoginLogVO;

/**
 * 登录日志服务接口。
 */
public interface LoginLogService {

    /**
     * 记录登录日志。
     *
     * @param logDO 登录日志实体
     */
    void recordLoginLog(LoginLogDO logDO);

    /**
     * 异步记录登录日志。
     *
     * @param logDO 登录日志实体
     */
    void recordLoginLogAsync(LoginLogDO logDO);

    /**
     * 查询登录日志列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<LoginLogVO> list(LoginLogQueryDTO query);
}
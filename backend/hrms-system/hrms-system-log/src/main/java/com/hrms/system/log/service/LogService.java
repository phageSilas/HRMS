package com.hrms.system.log.service;

/**
 * 日志审计服务接口
 */
public interface LogService {

    /**
     * 记录操作日志
     *
     * @param operation 操作描述
     * @param userId    用户ID
     */
    void recordOperation(String operation, Long userId);

    /**
     * 查询操作日志
     *
     * @param userId 用户ID
     * @return 日志内容
     */
    String queryOperationLog(Long userId);

}

package com.hrms.system.log.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.log.dto.OperateLogQueryDTO;
import com.hrms.system.log.entity.OperateLogDO;
import com.hrms.system.log.vo.OperateLogVO;

/**
 * 操作日志服务接口。
 */
public interface OperateLogService {

    /**
     * 异步记录操作日志。
     *
     * @param logDO 操作日志实体
     */
    void recordAsync(OperateLogDO logDO);

    /**
     * 同步记录操作日志。
     *
     * @param logDO 操作日志实体
     */
    void record(OperateLogDO logDO);

    /**
     * 查询操作日志列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<OperateLogVO> list(OperateLogQueryDTO query);
}
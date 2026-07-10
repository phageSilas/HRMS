package com.hrms.system.log.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.web.Result;
import com.hrms.system.log.dto.OperateLogQueryDTO;
import com.hrms.system.log.service.OperateLogService;
import com.hrms.system.log.vo.OperateLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器。
 */
@RestController
@RequestMapping("/operate-logs")
public class OperateLogController {

    @Autowired
    private OperateLogService operateLogService;

    /**
     * 查询操作日志列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<OperateLogVO>> list(OperateLogQueryDTO query) {
        Page<OperateLogVO> result = operateLogService.list(query);
        return Result.success(result);
    }
}
package com.hrms.system.log.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.web.Result;
import com.hrms.system.log.dto.LoginLogQueryDTO;
import com.hrms.system.log.service.LoginLogService;
import com.hrms.system.log.vo.LoginLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志控制器。
 */
@RestController
@RequestMapping("/login-logs")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    /**
     * 查询登录日志列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<LoginLogVO>> list(LoginLogQueryDTO query) {
        Page<LoginLogVO> result = loginLogService.list(query);
        return Result.success(result);
    }
}
package com.hrms.system.service.impl;

import com.hrms.system.service.SystemModuleService;
import org.springframework.stereotype.Service;

/**
 * 实现系统基础域的演示服务能力。
 */
@Service
public class SystemModuleServiceImpl implements SystemModuleService {

    /**
     * 获取系统基础域的模块说明。
     *
     * @return 模块说明信息
     */
    @Override
    public String getModuleSummary() {
        return "system module: permission, organization, employee archive";
    }
}

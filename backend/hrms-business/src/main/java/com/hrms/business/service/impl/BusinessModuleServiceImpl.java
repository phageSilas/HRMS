package com.hrms.business.service.impl;

import com.hrms.business.service.BusinessModuleService;
import com.hrms.system.service.SystemModuleService;
import org.springframework.stereotype.Service;

/**
 * 实现业务域的演示服务能力。
 */
@Service
public class BusinessModuleServiceImpl implements BusinessModuleService {

    private final SystemModuleService systemModuleService;

    /**
     * 创建业务域服务实现。
     *
     * @param systemModuleService 系统基础域服务
     */
    public BusinessModuleServiceImpl(SystemModuleService systemModuleService) {
        this.systemModuleService = systemModuleService;
    }

    /**
     * 获取业务域的模块说明。
     *
     * @return 模块说明信息
     */
    @Override
    public String getModuleSummary() {
        return "business module: onboarding, attendance, payroll, approval, profile; depends on "
            + systemModuleService.getModuleSummary();
    }
}

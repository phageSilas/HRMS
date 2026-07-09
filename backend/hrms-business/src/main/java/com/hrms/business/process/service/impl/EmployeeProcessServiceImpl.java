package com.hrms.business.process.service.impl;

import com.hrms.business.process.dto.EmployeeProcessStartRequestDTO;
import com.hrms.business.process.service.EmployeeProcessService;
import com.hrms.business.process.vo.EmployeeProcessVO;
import org.springframework.stereotype.Service;

/**
 * 实现员工入转调离流程业务接口能力。
 */
@Service
public class EmployeeProcessServiceImpl implements EmployeeProcessService {

    /**
     * 发起入职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @Override
    public EmployeeProcessVO startOnboarding(EmployeeProcessStartRequestDTO requestParam) {
        return buildProcess(requestParam, "ONBOARDING");
    }

    /**
     * 发起转正流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @Override
    public EmployeeProcessVO startRegularization(EmployeeProcessStartRequestDTO requestParam) {
        return buildProcess(requestParam, "REGULARIZATION");
    }

    /**
     * 发起调岗流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @Override
    public EmployeeProcessVO startTransfer(EmployeeProcessStartRequestDTO requestParam) {
        return buildProcess(requestParam, "TRANSFER");
    }

    /**
     * 发起离职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    @Override
    public EmployeeProcessVO startResignation(EmployeeProcessStartRequestDTO requestParam) {
        return buildProcess(requestParam, "RESIGNATION");
    }

    /**
     * 构建员工流程占位返回信息。
     *
     * @param requestParam 员工流程发起请求参数
     * @param processType 流程类型
     * @return 员工流程信息
     */
    private EmployeeProcessVO buildProcess(EmployeeProcessStartRequestDTO requestParam, String processType) {
        return new EmployeeProcessVO(1L, requestParam.employeeId(), processType, "PROCESSING");
    }
}

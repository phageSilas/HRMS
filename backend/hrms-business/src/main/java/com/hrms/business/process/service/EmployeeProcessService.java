package com.hrms.business.process.service;

import com.hrms.business.process.dto.EmployeeProcessStartRequestDTO;
import com.hrms.business.process.vo.EmployeeProcessVO;

/**
 * 定义员工入转调离流程业务接口能力。
 */
public interface EmployeeProcessService {

    /**
     * 发起入职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    EmployeeProcessVO startOnboarding(EmployeeProcessStartRequestDTO requestParam);

    /**
     * 发起转正流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    EmployeeProcessVO startRegularization(EmployeeProcessStartRequestDTO requestParam);

    /**
     * 发起调岗流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    EmployeeProcessVO startTransfer(EmployeeProcessStartRequestDTO requestParam);

    /**
     * 发起离职流程。
     *
     * @param requestParam 员工流程发起请求参数
     * @return 员工流程信息
     */
    EmployeeProcessVO startResignation(EmployeeProcessStartRequestDTO requestParam);
}

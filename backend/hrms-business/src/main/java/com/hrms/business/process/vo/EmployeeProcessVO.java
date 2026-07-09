package com.hrms.business.process.vo;

/**
 * 员工流程返回对象。
 *
 * @param processId 流程ID
 * @param employeeId 员工ID
 * @param processType 流程类型
 * @param approvalStatus 审批状态
 */
public record EmployeeProcessVO(
    Long processId,
    Long employeeId,
    String processType,
    String approvalStatus
) {
}

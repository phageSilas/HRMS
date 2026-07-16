package com.hrms.business.approval.dto;

import lombok.Data;

/**
 * 员工简要信息（仅供审批模块跨模块查询使用）
 */
@Data
public class EmployeeBriefDTO {

    /**
     * 员工ID
     */
    private Long id;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 关联系统用户ID
     */
    private Long userId;

    /**
     * 直接汇报人员工ID
     */
    private Long leaderId;

    /**
     * 所属部门ID
     */
    private Long deptId;
}

package com.hrms.business.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批联动员工档案更新参数
 * <p>
 * 用于人员模块在审批通过后按需同步员工档案字段。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeApprovalSyncUpdateDTO {

    /**
     * 在职状态
     */
    private Integer employmentStatus;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 职位ID
     */
    private Long postId;

    /**
     * 职级
     */
    private String jobLevel;

    /**
     * 汇报人员工ID
     */
    private Long leaderId;
}

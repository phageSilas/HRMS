package com.hrms.business.attendance.dto;

import lombok.Data;

import java.util.List;

/**
 * 考勤组适用范围请求参数。
 */
@Data
public class AttendanceGroupMemberRangeDTO {

    /**
     * 范围类型：DEPT-部门，POST-职位，EMPLOYEE-指定员工。
     */
    private String scopeType;

    /**
     * 部门范围 ID 列表。
     */
    private List<Long> deptIds;

    /**
     * 指定员工模式下用于筛选员工的部门 ID。
     */
    private Long deptId;

    /**
     * 职位范围 ID。
     */
    private Long postId;

    /**
     * 指定员工 ID 列表。
     */
    private List<Long> employeeIds;
}

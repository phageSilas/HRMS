package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假管理列表项视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLeaveManageItemVO {

    /**
     * 请假申请ID。
     */
    private Long id;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 员工工号。
     */
    private String employeeNo;

    /**
     * 部门ID。
     */
    private Long deptId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 请假类型。
     */
    private String leaveType;

    /**
     * 请假类型描述。
     */
    private String leaveTypeDesc;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 请假天数。
     */
    private BigDecimal totalDays;

    /**
     * 请假事由。
     */
    private String leaveReason;

    /**
     * 审批状态。
     */
    private Integer approvalStatus;

    /**
     * 审批状态描述。
     */
    private String approvalStatusDesc;

    /**
     * 审批实例ID。
     */
    private Long approvalInstanceId;

    /**
     * 当前审批节点名称。
     */
    private String currentNodeName;

    /**
     * 当前审批人名称。
     */
    private String currentApproverName;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}

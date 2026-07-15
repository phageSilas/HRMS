package com.hrms.business.attendance.vo;

import lombok.Data;

/**
 * 请假申请创建结果。
 */
@Data
public class LeaveCreateVO {

    /**
     * 请假申请ID。
     */
    private Long id;

    /**
     * 审批实例ID。
     */
    private Long approvalInstanceId;

    /**
     * 审批状态。
     */
    private Integer approvalStatus;
}

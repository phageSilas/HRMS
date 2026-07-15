package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请假申请创建结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

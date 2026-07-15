package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 补卡申请创建结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionCreateVO {

    /**
     * 补卡申请ID。
     */
    private Long id;

    /**
     * 打卡记录ID。
     */
    private Long recordId;

    /**
     * 审批实例ID。
     */
    private Long approvalInstanceId;

    /**
     * 审批状态。
     */
    private Integer approvalStatus;
}

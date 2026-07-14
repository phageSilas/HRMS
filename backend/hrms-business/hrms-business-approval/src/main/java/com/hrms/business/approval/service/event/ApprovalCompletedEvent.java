package com.hrms.business.approval.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审批完成事件
 * <p>
 * 审批实例最终通过或驳回时发布此事件。
 * 其他业务模块（入转调离、考勤、薪资等）通过 @EventListener 监听并执行业务回调。
 * </p>
 */
@Data
@AllArgsConstructor
public class ApprovalCompletedEvent {

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 审批类型编码
     */
    private String approvalType;

    /**
     * 业务主键ID
     */
    private Long bizId;

    /**
     * 最终状态：2-已通过 / 3-已驳回
     */
    private Integer instanceStatus;
}

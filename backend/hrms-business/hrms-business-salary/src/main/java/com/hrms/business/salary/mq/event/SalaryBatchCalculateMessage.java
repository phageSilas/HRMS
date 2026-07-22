package com.hrms.business.salary.mq.event;

import com.hrms.common.mq.HrmsMqMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪资批次核算消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchCalculateMessage implements HrmsMqMessage {

    private String messageId;

    private Long batchId;

    private String salaryMonth;

    private String triggerType;

    private String rollbackStatus;

    private Boolean applyAdjustments;
}

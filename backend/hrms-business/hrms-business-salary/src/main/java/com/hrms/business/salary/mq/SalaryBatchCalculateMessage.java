package com.hrms.business.salary.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪资批次核算消息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchCalculateMessage {

    private String messageId;

    private Long batchId;

    private String salaryMonth;
}

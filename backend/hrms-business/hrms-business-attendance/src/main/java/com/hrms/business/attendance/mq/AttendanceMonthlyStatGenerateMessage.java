package com.hrms.business.attendance.mq;

import com.hrms.common.mq.HrmsMqMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考勤月度统计生成消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMonthlyStatGenerateMessage implements HrmsMqMessage {

    /**
     * 消息唯一ID。
     */
    private String messageId;

    /**
     * 统计月份，格式 yyyy-MM。
     */
    private String month;

    /**
     * 员工ID列表，为空时按当前规则统计全量在职员工。
     */
    private List<Long> employeeIds;
}

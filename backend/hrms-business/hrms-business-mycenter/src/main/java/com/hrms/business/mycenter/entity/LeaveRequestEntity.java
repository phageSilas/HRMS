package com.hrms.business.mycenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 * 对应表 hr_leave_request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_leave_request")
public class LeaveRequestEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 请假类型：ANNUAL-年假 COMPASSIONATE-调休 SICK-病假 PERSONAL-事假
     */
    private String leaveType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 请假天数
     */
    private BigDecimal totalDays;

    /**
     * 请假小时数
     */
    private BigDecimal totalHours;

    /**
     * 请假原因
     */
    private String leaveReason;

    /**
     * 附件地址
     */
    private String attachmentUrl;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝 4-已撤回
     */
    private Integer approvalStatus;
}

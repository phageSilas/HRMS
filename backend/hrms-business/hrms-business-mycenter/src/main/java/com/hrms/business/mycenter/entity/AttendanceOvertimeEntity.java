package com.hrms.business.mycenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班申请实体
 * 对应表 hr_attendance_overtime
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_overtime")
public class AttendanceOvertimeEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 加班日期
     */
    private LocalDateTime overtimeDate;

    /**
     * 加班时长（小时）
     */
    private BigDecimal duration;

    /**
     * 加班事由
     */
    private String reason;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝
     */
    private Integer approvalStatus;
}

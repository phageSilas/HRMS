package com.hrms.business.mycenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 补卡申请实体
 * 对应表 hr_attendance_correction
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_correction")
public class AttendanceCorrectionEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 打卡记录ID
     */
    private Long recordId;

    /**
     * 补卡日期
     */
    private LocalDate correctionDate;

    /**
     * 补卡时间（用户指定的实际打卡时间）
     */
    private LocalTime correctionTime;

    /**
     * 补卡类型：CLOCK_IN-上班补卡 CLOCK_OUT-下班补卡
     */
    private String correctionType;

    /**
     * 补卡原因
     */
    private String correctionReason;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝
     */
    private Integer approvalStatus;
}

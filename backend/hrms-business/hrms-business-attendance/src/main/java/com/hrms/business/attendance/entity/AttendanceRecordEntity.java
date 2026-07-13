package com.hrms.business.attendance.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录实体
 */
@Data
public class AttendanceRecordEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 上班时间
     */
    private LocalDateTime checkInTime;

    /**
     * 下班时间
     */
    private LocalDateTime checkOutTime;

    /**
     * 考勤状态：1-正常，2-迟到，3-早退，4-缺勤，5-请假
     */
    private Integer status;

    /**
     * 工作时长（分钟）
     */
    private Integer workMinutes;

    /**
     * 备注
     */
    private String remark;

}

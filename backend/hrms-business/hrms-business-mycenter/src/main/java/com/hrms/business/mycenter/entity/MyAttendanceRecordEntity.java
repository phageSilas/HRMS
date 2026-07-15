package com.hrms.business.mycenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录（只读映射，用于个人中心日历展示）
 * 对应表 hr_attendance_record，不做写操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_record")
public class MyAttendanceRecordEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 打卡日期
     */
    private LocalDate recordDate;

    /**
     * 上班打卡时间
     */
    private LocalDateTime clockInTime;

    /**
     * 下班打卡时间
     */
    private LocalDateTime clockOutTime;

    /**
     * 上班状态：NORMAL/LATE/MISSING/ABSENCE
     */
    private String clockInStatus;

    /**
     * 下班状态：NORMAL/EARLY_LEAVE/MISSING/ABSENCE
     */
    private String clockOutStatus;

    /**
     * 补卡状态：NONE/PENDING/APPROVED
     */
    private String correctionStatus;
}

package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡记录实体，对应 hr_attendance_record。
 */
@Data
@TableName("hr_attendance_record")
public class AttendanceRecordEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 考勤组ID。
     */
    private Long groupId;

    /**
     * 打卡日期。
     */
    private LocalDate recordDate;

    /**
     * 上班打卡时间。
     */
    private LocalDateTime clockInTime;

    /**
     * 下班打卡时间。
     */
    private LocalDateTime clockOutTime;

    /**
     * 上班状态：NORMAL/LATE/MISSING/ABSENCE。
     */
    private String clockInStatus;

    /**
     * 下班状态：NORMAL/EARLY_LEAVE/MISSING/ABSENCE。
     */
    private String clockOutStatus;

    /**
     * 上班打卡 IP。
     */
    private String clockInIp;

    /**
     * 下班打卡 IP。
     */
    private String clockOutIp;

    /**
     * 上班打卡 GPS。
     */
    private String clockInGps;

    /**
     * 下班打卡 GPS。
     */
    private String clockOutGps;

    /**
     * 设备信息。
     */
    private String deviceInfo;

    /**
     * 补卡状态：NONE/PENDING/APPROVED。
     */
    private String correctionStatus;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}

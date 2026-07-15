package com.hrms.business.mycenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录（只读映射，用于个人中心日历展示）
 * 对应表 hr_attendance_record
 * 注意：此表不含 create_by/update_by/is_deleted/version 列，不继承 BaseEntity
 */
@Data
@TableName("hr_attendance_record")
public class MyAttendanceRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 打卡日期 */
    private LocalDate recordDate;

    /** 上班打卡时间 */
    private LocalDateTime clockInTime;

    /** 下班打卡时间 */
    private LocalDateTime clockOutTime;

    /** 上班状态：NORMAL/LATE/MISSING/ABSENCE */
    private String clockInStatus;

    /** 下班状态：NORMAL/EARLY_LEAVE/MISSING/ABSENCE */
    private String clockOutStatus;

    /** 补卡状态：NONE/PENDING/APPROVED */
    private String correctionStatus;

    /** 考勤组ID */
    private Long groupId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

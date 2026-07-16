package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 考勤组成员关系实体，对应 hr_attendance_group_member。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_group_member")
public class AttendanceGroupMemberEntity extends BaseEntity {

    /**
     * 考勤组ID。
     */
    private Long groupId;

    /**
     * 员工ID。
     */
    private Long employeeId;

    /**
     * 生效开始日期。
     */
    private LocalDate effectiveStartDate;

    /**
     * 生效结束日期，空表示长期有效。
     */
    private LocalDate effectiveEndDate;

    /**
     * 状态：0-停用 1-启用。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

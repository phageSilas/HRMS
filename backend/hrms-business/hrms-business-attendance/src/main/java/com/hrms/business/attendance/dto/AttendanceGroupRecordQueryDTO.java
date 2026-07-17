package com.hrms.business.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 考勤组打卡记录分页查询参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGroupRecordQueryDTO {

    /**
     * 查询月份，格式 yyyy-MM。
     */
    private String yearMonth;

    /**
     * 查询开始日期。
     */
    private LocalDate dateStart;

    /**
     * 查询结束日期。
     */
    private LocalDate dateEnd;

    /**
     * 关键词，匹配员工姓名或工号。
     */
    private String keyword;

    /**
     * 部门ID。
     */
    private Long departmentId;

    /**
     * 打卡状态。
     */
    private String status;

    /**
     * 当前页码。
     */
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页大小。
     */
    @Builder.Default
    private Integer pageSize = 10;
}

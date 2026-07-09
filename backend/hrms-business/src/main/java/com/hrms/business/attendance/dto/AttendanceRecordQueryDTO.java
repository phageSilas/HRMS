package com.hrms.business.attendance.dto;

import com.hrms.common.model.BasePageQuery;
import java.time.LocalDate;

/**
 * 考勤记录分页查询参数。
 */
public class AttendanceRecordQueryDTO extends BasePageQuery {

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 获取员工ID。
     *
     * @return 员工ID
     */
    public Long getEmployeeId() {
        return employeeId;
    }

    /**
     * 设置员工ID。
     *
     * @param employeeId 员工ID
     */
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * 获取开始日期。
     *
     * @return 开始日期
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * 设置开始日期。
     *
     * @param startDate 开始日期
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * 获取结束日期。
     *
     * @return 结束日期
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * 设置结束日期。
     *
     * @param endDate 结束日期
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

package com.hrms.business.attendance.service;

/**
 * 考勤管理服务接口
 */
public interface AttendanceService {

    /**
     * 获取月度考勤汇总
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份
     * @return 考勤汇总数据
     */
    Object getMonthlySummary(Long employeeId, Integer year, Integer month);

    /**
     * 打卡
     *
     * @param employeeId 员工ID
     * @param type       打卡类型：1-上班，2-下班
     */
    void checkIn(Long employeeId, Integer type);

    /**
     * 发起请假申请
     *
     * @param employeeId 员工ID
     * @param leaveType  请假类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param reason     请假原因
     */
    void applyLeave(Long employeeId, String leaveType, String startDate, String endDate, String reason);

}

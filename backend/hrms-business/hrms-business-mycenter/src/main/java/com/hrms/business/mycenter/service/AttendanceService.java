package com.hrms.business.mycenter.service;

import com.hrms.business.mycenter.dto.AttendanceCalendarVO;
import com.hrms.business.mycenter.dto.MakeupRecordVO;
import com.hrms.business.mycenter.dto.MakeupRequest;

import java.util.List;

/**
 * 个人考勤服务接口
 */
public interface AttendanceService {

    /**
     * 获取考勤日历
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月 yyyy-MM
     * @return 考勤日历
     */
    AttendanceCalendarVO getCalendar(Long employeeId, String yearMonth);

    /**
     * 打卡
     *
     * @param employeeId 员工ID
     * @param type       打卡类型：1-上班 2-下班
     */
    void clockIn(Long employeeId, Integer type);

    /**
     * 申请补卡
     *
     * @param employeeId 员工ID
     * @param request    补卡请求
     */
    void createMakeup(Long employeeId, MakeupRequest request);

    /**
     * 查询补卡记录列表
     *
     * @param employeeId 员工ID
     * @return 补卡记录列表
     */
    List<MakeupRecordVO> listMakeupRecords(Long employeeId);
}

package com.hrms.business.attendance.service;

import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.LeaveCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceLeaveManageQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupRecordQueryDTO;
import com.hrms.business.attendance.dto.MonthlyStatGenerateRequestDTO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.business.attendance.vo.AttendanceGroupRecordPageVO;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;
import com.hrms.business.attendance.vo.MonthlyStatGenerateVO;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.attendance.vo.AttendanceSummaryDashboardVO;
import com.hrms.business.attendance.vo.AttendanceLeaveManageItemVO;

import java.util.List;
import com.hrms.common.web.PageResult;

/**
 * 考勤管理服务接口。
 */
public interface AttendanceService {

    /**
     * 分页查询考勤组。
     *
     * @param queryDTO 查询参数
     * @return 考勤组分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<AttendanceGroupPageVO> pageAttendanceGroups(AttendanceGroupQueryDTO queryDTO);

    /**
     * 创建考勤组。
     *
     * @param requestDTO 创建请求
     * @return 创建后的考勤组
     * 本方法使用的工具类: 无
     */
    AttendanceGroupPageVO createAttendanceGroup(AttendanceGroupCreateOrUpdateRequestDTO requestDTO);

    /**
     * 更新考勤组。
     *
     * @param id         考勤组ID
     * @param requestDTO 更新请求
     * @return 更新后的考勤组
     * 本方法使用的工具类: 无
     */
    AttendanceGroupPageVO updateAttendanceGroup(Long id, AttendanceGroupCreateOrUpdateRequestDTO requestDTO);

    /**
     * 逻辑删除考勤组。
     *
     * @param id 考勤组ID
     * 本方法使用的工具类: 无
     */
    void deleteAttendanceGroup(Long id);

    /**
     * 分页查询考勤组打卡记录。
     *
     * @param groupId  考勤组ID
     * @param queryDTO 查询参数
     * @return 考勤组打卡记录分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<AttendanceGroupRecordPageVO> pageGroupAttendanceRecords(Long groupId,
                                                                       AttendanceGroupRecordQueryDTO queryDTO);

    /**
     * 当前登录员工打卡。
     *
     * @param requestDTO 打卡请求
     * @param clientIp   客户端 IP
     * @return 打卡结果
     * 本方法使用的工具类: 无
     */
    AttendanceClockVO clock(AttendanceClockRequestDTO requestDTO, String clientIp);

    /**
     * 查询当前员工个人月度打卡日历。
     *
     * @param yearMonth 月份，格式 yyyy-MM
     * @return 个人月度打卡日历
     * 本方法使用的工具类: 无
     */
    AttendanceCalendarVO getMyCalendar(String yearMonth);

    /**
     * 查询HR和主管考勤统计看板。
     *
     * @param yearMonth 月份，格式yyyy-MM
     * @param deptId    部门ID
     * @return 考勤统计看板
     * 本方法使用的工具类: 无
     */
    AttendanceSummaryDashboardVO getSummaryDashboard(String yearMonth, Long deptId, Boolean refreshCache);

    /**
     * 分页查询管理侧请假记录。
     *
     * @param queryDTO 查询参数
     * @return 请假管理分页列表
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<AttendanceLeaveManageItemVO> pageLeaveManageList(AttendanceLeaveManageQueryDTO queryDTO);

    /**
     * 快速审批通过请假申请。
     *
     * @param id 请假申请 ID
     */
    void quickApproveLeaveRequest(Long id);

    /**
     * 创建补卡申请。
     *
     * @param requestDTO 补卡申请请求
     * @return 补卡申请创建结果
     * 本方法使用的工具类: 无
     */
    AttendanceCorrectionCreateVO createCorrection(AttendanceCorrectionCreateRequestDTO requestDTO);

    /**
     * 查询启用的请假类型。
     *
     * @return 请假类型列表
     * 本方法使用的工具类: List(JDK)
     */
    List<LeaveTypeVO> listLeaveTypes();

    /**
     * 查询当前员工假期余额。
     *
     * @return 假期余额列表
     * 本方法使用的工具类: List(JDK)
     */
    List<LeaveBalanceVO> listLeaveBalances();

    /**
     * 提交请假申请。
     *
     * @param requestDTO 请假申请请求
     * @return 请假申请创建结果
     * 本方法使用的工具类: 无
     */
    LeaveCreateVO createLeave(LeaveCreateRequestDTO requestDTO);

    /**
     * 生成月度考勤统计。
     *
     * @param requestDTO 生成请求
     * @return 生成结果
     * 本方法使用的工具类: 无
     */
    MonthlyStatGenerateVO generateMonthlyStat(MonthlyStatGenerateRequestDTO requestDTO);

    /**
     * 查询薪资模块月度考勤数据源。
     *
     * @param month       月份
     * @param employeeIds 员工ID列表
     * @return 月度考勤汇总
     * 本方法使用的工具类: List(JDK)
     */
    List<AttendancePayrollSourceVO> getPayrollSource(String month, List<Long> employeeIds);

}

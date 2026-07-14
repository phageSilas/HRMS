package com.hrms.business.attendance.service;

import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.LeaveCreateRequestDTO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;

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
     * 获取月度考勤汇总。
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份
     * @return 考勤汇总数据
     * 本方法使用的工具类: 无
     */
    Object getMonthlySummary(Long employeeId, Integer year, Integer month);

    /**
     * 打卡。
     *
     * @param employeeId 员工ID
     * @param type       打卡类型：1-上班，2-下班
     * 本方法使用的工具类: 无
     */
    void checkIn(Long employeeId, Integer type);

    /**
     * 发起请假申请。
     *
     * @param employeeId 员工ID
     * @param leaveType  请假类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param reason     请假原因
     * 本方法使用的工具类: 无
     */
    void applyLeave(Long employeeId, String leaveType, String startDate, String endDate, String reason);
}

package com.hrms.business.attendance.service;

import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
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

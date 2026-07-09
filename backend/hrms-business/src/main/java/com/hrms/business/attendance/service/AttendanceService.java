package com.hrms.business.attendance.service;

import com.hrms.business.attendance.dto.AttendanceRecordQueryDTO;
import com.hrms.business.attendance.dto.LeaveApplyRequestDTO;
import com.hrms.business.attendance.vo.AttendanceRecordVO;
import com.hrms.business.attendance.vo.LeaveApplyVO;
import com.hrms.common.model.PageResult;

/**
 * 定义考勤业务接口能力。
 */
public interface AttendanceService {

    /**
     * 分页查询考勤记录。
     *
     * @param queryParam 考勤记录查询参数
     * @return 考勤记录分页结果
     */
    PageResult<AttendanceRecordVO> pageRecords(AttendanceRecordQueryDTO queryParam);

    /**
     * 提交请假申请。
     *
     * @param requestParam 请假申请请求参数
     * @return 请假申请结果
     */
    LeaveApplyVO applyLeave(LeaveApplyRequestDTO requestParam);
}

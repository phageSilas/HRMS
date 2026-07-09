package com.hrms.business.attendance.service.impl;

import com.hrms.business.attendance.dto.AttendanceRecordQueryDTO;
import com.hrms.business.attendance.dto.LeaveApplyRequestDTO;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceRecordVO;
import com.hrms.business.attendance.vo.LeaveApplyVO;
import com.hrms.common.model.PageResult;
import org.springframework.stereotype.Service;

/**
 * 实现考勤业务接口能力。
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    /**
     * 分页查询考勤记录。
     *
     * @param queryParam 考勤记录查询参数
     * @return 考勤记录分页结果
     */
    @Override
    public PageResult<AttendanceRecordVO> pageRecords(AttendanceRecordQueryDTO queryParam) {
        return PageResult.empty(queryParam.getPageNum(), queryParam.getPageSize());
    }

    /**
     * 提交请假申请。
     *
     * @param requestParam 请假申请请求参数
     * @return 请假申请结果
     */
    @Override
    public LeaveApplyVO applyLeave(LeaveApplyRequestDTO requestParam) {
        return new LeaveApplyVO(1L, "PROCESSING");
    }
}

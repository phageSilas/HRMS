package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.LeaveApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.LeaveApplicationQueryDTO;
import com.hrms.business.personnel.vo.LeaveApplicationCreateVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.common.web.PageResult;

/**
 * 离职申请服务接口
 */
public interface LeaveApplicationService {

    /**
     * 分页查询离职申请列表。
     *
     * @param queryDTO 离职申请查询参数
     * @return 离职申请分页结果
     * 本方法使用的工具类: 无
     */
    PageResult<LeaveApplicationPageVO> pageLeaveApplications(LeaveApplicationQueryDTO queryDTO);

    /**
     * 创建离职申请。
     *
     * @param requestDTO 离职申请创建参数
     * @return 离职申请创建结果
     * 本方法使用的工具类: 无
     */
    LeaveApplicationCreateVO createLeaveApplication(LeaveApplicationCreateRequestDTO requestDTO);

    /**
     * 快速审批通过离职申请。
     *
     * @param id 离职申请ID
     */
    void quickApproveLeaveApplication(Long id);

}

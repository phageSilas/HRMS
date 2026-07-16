package com.hrms.business.mycenter.service;

import com.hrms.business.mycenter.dto.LeaveBalanceVO;
import com.hrms.business.mycenter.dto.LeaveRequestDTO;
import com.hrms.business.mycenter.dto.LeaveVO;

import java.util.List;

/**
 * 请假服务接口
 */
public interface LeaveService {

    /**
     * 提交请假申请
     *
     * @param employeeId 员工ID
     * @param request    请假请求
     */
    void createLeave(Long employeeId, LeaveRequestDTO request);

    /**
     * 查询请假记录
     *
     * @param employeeId 员工ID
     * @return 请假记录列表
     */
    List<LeaveVO> listLeaves(Long employeeId);

    /**
     * 取消请假
     *
     * @param employeeId 员工ID
     * @param leaveId    请假ID
     */
    void cancelLeave(Long employeeId, Long leaveId);

    /**
     * 查询假期余额
     *
     * @param employeeId 员工ID
     * @return 假期余额
     */
    LeaveBalanceVO getLeaveBalance(Long employeeId);
}

package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.RegularApplicationApplyRequestDTO;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.vo.RegularApplicationApplyVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.web.PageResult;

/**
 * 转正申请服务接口
 */
public interface RegularApplicationService {

    /**
     * 分页查询转正申请列表。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: 无
     */
    PageResult<RegularApplicationPageVO> pageRegularApplications(RegularApplicationQueryDTO queryDTO);

    /**
     * 发起转正评估。
     *
     * @param employeeId 员工ID
     * @param requestDTO 转正评估请求
     * @return 转正评估发起结果
     * 本方法使用的工具类: 无
     */
    RegularApplicationApplyVO applyRegular(Long employeeId, RegularApplicationApplyRequestDTO requestDTO);

    /**
     * 快速审批通过转正申请。
     *
     * @param id 转正申请ID
     */
    void quickApproveRegularApplication(Long id);

}

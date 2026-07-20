package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.EntryApplicationConfirmRequestDTO;
import com.hrms.business.personnel.dto.EntryApplicationQueryDTO;
import com.hrms.business.personnel.dto.EntryApplicationCreateOrUpdateRequestDTO;
import com.hrms.business.personnel.vo.EntryApplicationConfirmVO;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.EntryApplicationSubmitVO;
import com.hrms.common.web.PageResult;

/**
 * 入职申请服务接口
 */
public interface EntryApplicationService {

    /**
     * 分页查询入职申请列表。
     *
     * @param queryDTO 入职申请查询参数
     * @return 入职申请分页结果
     */
    PageResult<EntryApplicationPageVO> pageEntryApplications(EntryApplicationQueryDTO queryDTO);

    /**
     * 获取入职申请详情。
     *
     * @param id 入职申请ID
     * @return 入职申请详情
     */
    EntryApplicationPageVO getEntryApplication(Long id);

    /**
     * 创建入职申请草稿。
     *
     * @param requestDTO 入职申请创建参数
     * @return 入职申请分页 VO
     */
    EntryApplicationPageVO createEntryApplication(EntryApplicationCreateOrUpdateRequestDTO requestDTO);

    /**
     * 更新入职申请草稿。
     *
     * @param id 入职申请ID
     * @param requestDTO 入职申请更新参数
     */
    EntryApplicationPageVO updateEntryApplication(Long id, EntryApplicationCreateOrUpdateRequestDTO requestDTO);

    /**
     * 提交入职申请审批。
     *
     * @param id 入职申请ID
     * @return 提交审批结果
     */
    EntryApplicationSubmitVO submitEntryApplication(Long id);

    /**
     * 确认入职并触发员工档案创建。
     *
     * @param id 入职申请ID
     * @param requestDTO 入职确认参数
     * @return 入职确认结果
     */
    EntryApplicationConfirmVO confirmEntryApplication(Long id, EntryApplicationConfirmRequestDTO requestDTO);

}
